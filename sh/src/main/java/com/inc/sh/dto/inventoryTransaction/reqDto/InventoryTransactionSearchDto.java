package com.inc.sh.dto.inventoryTransaction.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionSearchDto {
    
    private String startDate;           // 조회시작일 (YYYYMMDD)
    private String endDate;             // 조회종료일 (YYYYMMDD)
    private Integer warehouseCode;      // 창고코드 (선택, 완전일치)
    private Integer categoryCode;       // 분류코드 (선택, 완전일치)
    private String itemCodeSearch;      // 품목코드 검색어 (선택, 품목코드만 부분검색)
}