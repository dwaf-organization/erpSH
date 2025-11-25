package com.inc.sh.dto.inventory.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItemDto {
    
    private Integer warehouseItemCode; // 창고품목코드 (기존 데이터일 경우)
    private Integer itemCode;          // 품목코드
    private Integer actualQuantity;    // 실사수량
    private Integer actualUnitPrice;   // 실사단가
    private Integer actualAmount;      // 실사금액
    private Integer safeQuantity;      // 안전재고량
}