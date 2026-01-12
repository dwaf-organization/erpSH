package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesReqDto {
    
    private Integer hqCode;          // 본사코드 (필수)
    private Integer brandCode;      // 브랜드코드 (0이면 전체)
    private String searchDate;      // 조회월 (YYYYMM 형식, 예: "202501")
    
}