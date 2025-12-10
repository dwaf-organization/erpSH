package com.inc.sh.dto.orderLimit.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitSearchDto {
    
    private Integer categoryCode; // 품목분류코드 (완전일치)
    private String itemName;      // 품명 (부분일치)
    private Integer hqCode;       // 본사코드
}