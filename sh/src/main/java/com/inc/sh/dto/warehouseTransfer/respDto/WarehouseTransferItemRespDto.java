package com.inc.sh.dto.warehouseTransfer.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferItemRespDto {
    
    private Integer itemCode;           // 품목코드
    private String itemName;            // 품명
    private String specification;       // 규격
    private String unit;                // 단위
    private Integer unitPrice;          // 단가
    private Integer quantity;           // 이송수량
    private Integer amount;             // 금액
}