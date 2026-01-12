package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodReviewReqDto {
    
    /**
     * 거래처코드 (본사코드)
     */
    private Integer hqCode;
    
    /**
     * 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     */
    private Integer brandCode;
    
    /**
     * 앱 이름 (배민/요기요/쿠팡이츠/전체)
     */
    private String appName;
    
    /**
     * 시작년월 (YYYYMM)
     */
    private String startYm;
    
    /**
     * 종료년월 (YYYYMM)  
     */
    private String endYm;
}