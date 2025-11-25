package com.inc.sh.dto.inventoryInspection.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInspectionSearchDto {
    
    private String closingYm;           // 마감년월 (필수)
    private Integer warehouseCode;      // 창고코드 (완전일치)
    private String itemSearch;          // 품목 검색어 (품목코드 또는 품명)
}