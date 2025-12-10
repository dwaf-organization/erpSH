package com.inc.sh.dto.inventory.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryDeleteReqDto {
    
    private List<Integer> warehouseItemCodes;  // 삭제할 창고품목코드 배열
}