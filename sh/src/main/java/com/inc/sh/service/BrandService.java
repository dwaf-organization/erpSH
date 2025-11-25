package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.brand.reqDto.BrandReqDto;
import com.inc.sh.dto.brand.respDto.BrandRespDto;
import com.inc.sh.entity.BrandInfo;
import com.inc.sh.repository.BrandRepository;
import com.inc.sh.repository.HeadquarterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
                brands = brandRepository.findByHqCode(hqCode);
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
     * 브랜드 저장 (신규/수정)
     * brandCode가 null이면 신규, 아니면 수정
     */
    @Transactional
    public RespDto<BrandRespDto> saveBrand(BrandReqDto request) {
        try {
            // 본사 존재 확인
            if (!headquarterRepository.existsById(request.getHqCode())) {
                return RespDto.fail("존재하지 않는 본사입니다: " + request.getHqCode());
            }

            BrandInfo brand;

            if (request.getBrandCode() == null) {
                // 신규 등록
                brand = BrandInfo.builder()
                        .hqCode(request.getHqCode())
                        .brandName(request.getBrandName())
                        .note(request.getNote())
                        .build();
                
                brand = brandRepository.save(brand);
                
                return RespDto.success("브랜드가 성공적으로 등록되었습니다.", BrandRespDto.from(brand));

            } else {
                // 수정
                brand = brandRepository.findById(request.getBrandCode())
                        .orElse(null);

                if (brand == null) {
                    return RespDto.fail("존재하지 않는 브랜드입니다: " + request.getBrandCode());
                }

                // hqCode 확인
                if (!brand.getHqCode().equals(request.getHqCode())) {
                    return RespDto.fail("브랜드의 본사코드가 일치하지 않습니다.");
                }

                // 브랜드명과 비고만 수정
                brand.setBrandName(request.getBrandName());
                brand.setNote(request.getNote());
                
                brand = brandRepository.save(brand);
                
                return RespDto.success("브랜드가 성공적으로 수정되었습니다.", BrandRespDto.from(brand));
            }

        } catch (Exception e) {
            return RespDto.fail("브랜드 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 브랜드 삭제
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