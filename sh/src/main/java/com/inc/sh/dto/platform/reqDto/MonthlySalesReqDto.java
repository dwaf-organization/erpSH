package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySalesReqDto {
    
    private Integer hqCode;         // 본사코드 (필수)
    private Integer brandCode;      // 브랜드코드 (0이면 전체)
    private String startDate;       // 시작월 (YYYYMM 형식, 예: "202401")
    private String endDate;         // 종료월 (YYYYMM 형식, 예: "202412")
    
}