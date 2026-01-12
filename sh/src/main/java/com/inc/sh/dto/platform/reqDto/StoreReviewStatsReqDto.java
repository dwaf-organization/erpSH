package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReviewStatsReqDto {
    
    /**
     * 거래처코드 (본사코드)
     */
    private Integer hqCode;
    
    /**
     * 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     */
    private Integer brandCode;
    
    /**
     * 시작년월 (YYYYMM)
     */
    private String startYearMonth;
    
    /**
     * 종료년월 (YYYYMM)  
     */
    private String endYearMonth;
}