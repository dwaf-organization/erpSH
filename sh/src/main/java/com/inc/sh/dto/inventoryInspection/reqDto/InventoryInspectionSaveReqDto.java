package com.inc.sh.dto.inventoryInspection.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInspectionSaveReqDto {
    
    private List<InventoryInspectionItemDto> updates;  // 수정할 재고실사 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryInspectionItemDto {
        private Integer closingCode;       // 마감코드 (closing_code) (필수)
        private Integer safeQuantity;
        private Integer actualQuantity;    // 실사수량
        private Integer actualUnitPrice;   // 실사단가
    }
}