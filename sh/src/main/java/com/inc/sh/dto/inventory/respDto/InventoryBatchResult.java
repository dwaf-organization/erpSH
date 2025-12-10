package com.inc.sh.dto.inventory.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<InventoryDeleteSuccessDto> successData; // 성공한 재고 데이터들
    private List<InventoryErrorDto> failData;           // 실패한 재고 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryDeleteSuccessDto {
        private Integer warehouseItemCode;              // 삭제된 창고품목코드
        private Integer itemCode;                       // 품목코드
        private String itemName;                        // 품명
        private String warehouseName;                   // 창고명
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryErrorDto {
        private Integer warehouseItemCode;              // 실패한 창고품목코드
        private String itemName;                        // 실패한 품명
        private String warehouseName;                   // 창고명
        private String errorMessage;                    // 실패 사유
    }
}