package com.inc.sh.dto.inventory.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySaveDto {
    
    private String closingYm;                    // 마감년월
    private Integer warehouseCode;               // 창고코드
    private List<InventoryItemDto> items;        // 품목 리스트
}