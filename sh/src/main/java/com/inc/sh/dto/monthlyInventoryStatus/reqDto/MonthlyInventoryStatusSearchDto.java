package com.inc.sh.dto.monthlyInventoryStatus.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyInventoryStatusSearchDto {
    
    private String closingYm;           // 마감년월 (필수)
    private Integer warehouseCode;      // 창고코드 (선택, 완전일치)
    private Integer categoryCode;       // 분류코드 (선택, 완전일치)
    private String itemSearch;          // 품목 검색어 (품목코드 부분검색)
}