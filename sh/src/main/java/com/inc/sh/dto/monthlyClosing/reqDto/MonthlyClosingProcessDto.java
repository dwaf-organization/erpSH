package com.inc.sh.dto.monthlyClosing.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyClosingProcessDto {
    
    private String closingYm;       // 마감년월
    private Integer warehouseCode;  // 창고코드
}