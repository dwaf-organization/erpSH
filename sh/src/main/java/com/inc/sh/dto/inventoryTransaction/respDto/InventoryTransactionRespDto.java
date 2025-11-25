package com.inc.sh.dto.inventoryTransaction.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionRespDto {
    
    private Integer warehouseCode;      // 창고코드
    private String categoryName;        // 분류명 (대분류명 또는 대분류명-중분류명)
    private Integer itemCode;           // 품목코드
    private String itemName;            // 품명
    private String specification;       // 규격
    private String unit;                // 단위
    private String transactionDate;     // 일자 (YYYY-MM-DD)
    private String transactionType;     // 조정유형
    private Integer quantity;           // 수량
    private Integer unitPrice;          // 단가
    private Integer amount;             // 금액
}