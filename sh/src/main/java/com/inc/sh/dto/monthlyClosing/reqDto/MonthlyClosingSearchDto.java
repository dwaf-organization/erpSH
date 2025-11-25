package com.inc.sh.dto.monthlyClosing.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyClosingSearchDto {
    
    private String closingYm;           // 마감년월 (필수)
    private Integer warehouseCode;      // 창고코드 (선택)
}