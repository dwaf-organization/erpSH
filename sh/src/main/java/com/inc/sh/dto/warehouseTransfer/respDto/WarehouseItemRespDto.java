package com.inc.sh.dto.warehouseTransfer.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseItemRespDto {
    
    private Integer itemCode;           // 품목코드
    private String itemName;            // 품명
    private String specification;       // 규격
    private String unit;                // 단위
    private Integer currentQuantity;    // 현재고량
    private Integer unitPrice;          // 단가 (item 테이블의 base_price)
}