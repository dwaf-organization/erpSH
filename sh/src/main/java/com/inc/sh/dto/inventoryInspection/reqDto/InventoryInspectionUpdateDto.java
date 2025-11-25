package com.inc.sh.dto.inventoryInspection.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInspectionUpdateDto {
    
    private Integer closingCode;       // 마감코드 (closing_code)
    private Integer actualQuantity;    // 실사수량
    private Integer actualUnitPrice;   // 실사단가
}