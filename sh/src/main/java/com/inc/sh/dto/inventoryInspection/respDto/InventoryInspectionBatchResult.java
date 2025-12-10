package com.inc.sh.dto.inventoryInspection.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInspectionBatchResult {
    
    private int totalCount;                                    // 총 처리 건수
    private int successCount;                                  // 성공 건수
    private int failCount;                                     // 실패 건수
    private List<InventoryInspectionSuccessDto> successData;   // 성공한 실사 데이터들
    private List<InventoryInspectionErrorDto> failData;       // 실패한 실사 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryInspectionSuccessDto {
        private Integer closingCode;                           // 마감코드
        private Integer itemCode;                              // 품목코드
        private String itemName;                               // 품명
        private Integer actualQuantity;                        // 실사수량
        private Integer actualUnitPrice;                       // 실사단가
        private Integer actualAmount;                          // 실사금액 (계산결과)
        private Integer diffQuantity;                          // 오차수량 (계산결과)
        private Integer diffAmount;                            // 오차금액 (계산결과)
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryInspectionErrorDto {
        private Integer closingCode;                           // 실패한 마감코드
        private String itemName;                               // 실패한 품명
        private String errorMessage;                           // 실패 사유
    }
}