package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSalesReqDto {
    
    private Integer hqCode;         // 본사코드 (필수)
    private Integer brandCode;      // 브랜드코드 (0=전체브랜드)
    
}