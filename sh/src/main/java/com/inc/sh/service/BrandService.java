package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.brand.reqDto.BrandReqDto;
import com.inc.sh.dto.brand.reqDto.BrandSaveReqDto;
import com.inc.sh.dto.brand.reqDto.BrandDeleteReqDto;
import com.inc.sh.dto.brand.respDto.BrandBatchResult;
import com.inc.sh.dto.brand.respDto.BrandRespDto;
import com.inc.sh.entity.BrandInfo;
import com.inc.sh.repository.BrandRepository;
import com.inc.sh.repository.HeadquarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {

    private final BrandRepository brandRepository;
    private final HeadquarterRepository headquarterRepository;

    /**
     * 브랜드 목록 조회
     * brandCode가 0이면 전체 조회, 아니면 해당 브랜드만 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<BrandRespDto>> getBrandList(Integer hqCode, Integer brandCode) {
        try {
            // 본사 존재 확인
            if (!headquarterRepository.existsById(hqCode)) {
                return RespDto.fail("존재하지 않는 본사입니다: " + hqCode);
            }

            List<BrandInfo> brands;
            
            if (brandCode == 0) {
                // 전체 조회
                brands = brandRepository.findByHqCodeOrderByBrandCodeDesc(hqCode);
            } else {
                // 특정 브랜드 조회
                BrandInfo brand = brandRepository.findByHqCodeAndBrandCode(hqCode, brandCode)
                        .orElse(null);
                brands = brand != null ? List.of(brand) : List.of();
            }

            List<BrandRespDto> respList = brands.stream()
                    .map(BrandRespDto::from)
                    .collect(Collectors.toList());

            return RespDto.success("브랜드 목록 조회 성공", respList);

        } catch (Exception e) {
            return RespDto.fail("브랜드 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    /**
     * 브랜드 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<BrandBatchResult> saveBrands(BrandSaveReqDto reqDto) {
        
        log.info("브랜드 다중 저장 시작 - 총 {}건", reqDto.getBrands().size());
        
        List<BrandRespDto> successData = new ArrayList<>();
        List<BrandBatchResult.BrandErrorDto> failData = new ArrayList<>();
        
        for (BrandSaveReqDto.BrandItemDto item : reqDto.getBrands()) {
            try {
                // 개별 브랜드 저장 처리
                BrandRespDto savedBrand = saveSingleBrand(item);
                successData.add(savedBrand);
                
                log.info("브랜드 저장 성공 - brandCode: {}, brandName: {}", 
                        savedBrand.getBrandCode(), savedBrand.getBrandName());
                
            } catch (Exception e) {
                log.error("브랜드 저장 실패 - brandName: {}, 에러: {}", item.getBrandName(), e.getMessage());
                
                BrandBatchResult.BrandErrorDto errorDto = BrandBatchResult.BrandErrorDto.builder()
                        .brandCode(item.getBrandCode())
                        .brandName(item.getBrandName())
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        BrandBatchResult result = BrandBatchResult.builder()
                .totalCount(reqDto.getBrands().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("브랜드 저장 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("브랜드 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getBrands().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 브랜드 저장 처리
     */
    private BrandRespDto saveSingleBrand(BrandSaveReqDto.BrandItemDto item) {
        
        // 본사 존재 확인
        if (!headquarterRepository.existsById(item.getHqCode())) {
            throw new RuntimeException("존재하지 않는 본사입니다: " + item.getHqCode());
        }
        
        BrandInfo brand;
        
        if (item.getBrandCode() == null) {
            // 신규 등록
            brand = BrandInfo.builder()
                    .hqCode(item.getHqCode())
                    .brandName(item.getBrandName())
                    .note(item.getNote())
                    .build();
            
            brand = brandRepository.save(brand);
            
        } else {
            // 수정
            brand = brandRepository.findById(item.getBrandCode())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 브랜드입니다: " + item.getBrandCode()));
            
            // hqCode 확인
            if (!brand.getHqCode().equals(item.getHqCode())) {
                throw new RuntimeException("브랜드의 본사코드가 일치하지 않습니다.");
            }
            
            // 브랜드명과 비고만 수정
            brand.setBrandName(item.getBrandName());
            brand.setNote(item.getNote());
            
            brand = brandRepository.save(brand);
        }
        
        return BrandRespDto.from(brand);
    }
    /**
     * 브랜드 단일 삭제 (호환성 유지용)
     */
    @Transactional
    public RespDto<String> deleteBrandSingle(Integer brandCode) {
        try {
            // 브랜드 존재 확인
            BrandInfo brand = brandRepository.findById(brandCode)
                    .orElse(null);

            if (brand == null) {
                return RespDto.fail("존재하지 않는 브랜드입니다: " + brandCode);
            }

            // 참조 테이블 확인
            String validationResult = validateBrandDeletion(brandCode);
            if (validationResult != null) {
                return RespDto.fail(validationResult);
            }

            // 삭제
            brandRepository.delete(brand);
            
            return RespDto.success("브랜드가 성공적으로 삭제되었습니다.", "삭제 완료");

        } catch (Exception e) {
            return RespDto.fail("브랜드 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 브랜드 다중 삭제 (새로 추가된 메서드)
     */
    @Transactional
    public RespDto<String> deleteBrandMultiple(BrandDeleteReqDto deleteDto) {
        
        try {
            log.info("브랜드 다중 삭제 시작 - 삭제 대상: {} 건", deleteDto.getBrandCodes().size());
            
            List<Integer> successDeletes = new ArrayList<>();
            List<String> failedDeletes = new ArrayList<>();
            
            // brandCodes 배열 순회하며 개별 삭제 처리
            for (Integer brandCode : deleteDto.getBrandCodes()) {
                try {
                    // 브랜드 존재 확인
                    BrandInfo brand = brandRepository.findById(brandCode)
                            .orElse(null);
                    
                    if (brand == null) {
                        failedDeletes.add(brandCode + "(존재하지 않음)");
                        continue;
                    }

                    // 참조 테이블 사용 여부 확인
                    String validationResult = validateBrandDeletion(brandCode);
                    if (validationResult != null) {
                        failedDeletes.add(brandCode + "(" + validationResult + ")");
                        continue;
                    }
                    
                    // 삭제 실행
                    brandRepository.delete(brand);
                    successDeletes.add(brandCode);
                    
                    log.info("브랜드 삭제 완료 - brandCode: {}", brandCode);
                    
                } catch (Exception e) {
                    log.error("브랜드 삭제 중 오류 발생 - brandCode: {}, 에러: {}", brandCode, e.getMessage());
                    failedDeletes.add(brandCode + "(삭제 오류)");
                }
            }
            
            // 성공 메시지 생성
            String message = String.format("브랜드 삭제 완료 - 성공: %d건, 실패: %d건", 
                    successDeletes.size(), failedDeletes.size());
            
            if (!failedDeletes.isEmpty()) {
                message += " | 실패 목록: " + String.join(", ", failedDeletes);
            }
            
            log.info("브랜드 다중 삭제 완료 - 총 처리: {}/{}건", 
                    successDeletes.size(), deleteDto.getBrandCodes().size());
            
            return RespDto.success(message, "삭제 완료");
            
        } catch (Exception e) {
            log.error("브랜드 다중 삭제 중 오류 발생", e);
            return RespDto.fail("브랜드 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 브랜드 삭제 유효성 검사 (참조 테이블 확인)
     */
    private String validateBrandDeletion(Integer brandCode) {
        
        // customer 테이블 확인
        Long customerCount = brandRepository.countCustomersByBrandCode(brandCode);
        if (customerCount > 0) {
            return "거래처 " + customerCount + "개 존재";
        }

        // order_limit_set 테이블 확인
        Long orderLimitCount = brandRepository.countOrderLimitSetByBrandCode(brandCode);
        if (orderLimitCount > 0) {
            return "주문제한설정 " + orderLimitCount + "개 존재";
        }

        return null; // 삭제 가능
    }

    /**
     * 브랜드 삭제 (기존 메서드 - 호환성 유지)
     */
    @Transactional
    public RespDto<Void> deleteBrand(Integer brandCode) {
        try {
            // 브랜드 존재 확인
            BrandInfo brand = brandRepository.findById(brandCode)
                    .orElse(null);

            if (brand == null) {
                return RespDto.fail("존재하지 않는 브랜드입니다: " + brandCode);
            }

            // 브랜드 사용 여부 확인 (customer 테이블)
            Long customerCount = brandRepository.countCustomersByBrandCode(brandCode);
            if (customerCount > 0) {
                return RespDto.fail("해당 브랜드를 사용하는 거래처가 " + customerCount + "개 존재하여 삭제할 수 없습니다.");
            }

            // 브랜드 사용 여부 확인 (order_limit_set 테이블)
            Long orderLimitCount = brandRepository.countOrderLimitSetByBrandCode(brandCode);
            if (orderLimitCount > 0) {
                return RespDto.fail("해당 브랜드를 사용하는 주문제한설정이 " + orderLimitCount + "개 존재하여 삭제할 수 없습니다.");
            }

            // 삭제
            brandRepository.delete(brand);

            return RespDto.success("브랜드가 성공적으로 삭제되었습니다.", null);

        } catch (Exception e) {
            return RespDto.fail("브랜드 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}