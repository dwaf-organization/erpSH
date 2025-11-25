package com.inc.sh.dto.inventory.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryRespDto {
    
    private Integer warehouseItemCode; // 창고품목코드
    private Integer itemCode;          // 품목코드
    private String itemName;           // 품명
    private String specification;      // 규격
    private String unit;               // 단위
    private Integer actualUnitPrice;   // 재고단가
    private Integer actualQuantity;    // 재고수량
    private Integer actualAmount;      // 재고금액
    private Integer currentQuantity;   // 현재고량
    private Integer safeQuantity;      // 안전재고량
    private String warehouseName;      // 창고명
    private String closingYm;          // 마감년월
}