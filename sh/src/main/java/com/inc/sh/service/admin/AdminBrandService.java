package com.inc.sh.service.admin;

import com.inc.sh.dto.brand.respDto.AdminBrandListRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.BrandRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBrandService {
    
    private final BrandRepository brandRepository;  // 관리자

    /**
     * 관리자 - 브랜드 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<AdminBrandListRespDto>> getBrandList(Integer hqCode) {
        try {
            String searchType = (hqCode == null) ? "전체" : "본사별";
            log.info("관리자 브랜드 목록 조회 시작 - 조회타입: {}, hqCode: {}", searchType, hqCode);
            
            List<Object[]> results = brandRepository.findBrandsForAdmin(hqCode);
            
            List<AdminBrandListRespDto> responseList = results.stream()
                    .map(result -> AdminBrandListRespDto.builder()
                            .brandCode((Integer) result[0])
                            .brandName((String) result[1])
                            .hqCode((Integer) result[2])
                            .hqName((String) result[3])
                            .note((String) result[4])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("관리자 브랜드 목록 조회 완료 - 조회타입: {}, hqCode: {}, 조회 건수: {}", 
                    searchType, hqCode, responseList.size());
            
            String message = String.format("브랜드 목록 조회 성공 (%s)", searchType);
            return RespDto.success(message, responseList);
            
        } catch (Exception e) {
            log.error("관리자 브랜드 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("브랜드 목록 조회 중 오류가 발생했습니다.");
        }
    }
}