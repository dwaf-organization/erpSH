package com.inc.sh.dto.brand.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandBatchResult {
    
    private int totalCount;                         // 총 처리 건수
    private int successCount;                       // 성공 건수
    private int failCount;                          // 실패 건수
    private List<BrandRespDto> successData;         // 성공한 브랜드 데이터들
    private List<BrandErrorDto> failData;           // 실패한 브랜드 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrandErrorDto {
        private Integer brandCode;                  // 실패한 브랜드코드
        private String brandName;                   // 실패한 브랜드명  
        private String errorMessage;                // 실패 사유
    }
}