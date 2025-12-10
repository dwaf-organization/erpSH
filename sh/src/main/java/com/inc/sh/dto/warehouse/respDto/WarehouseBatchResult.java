package com.inc.sh.dto.warehouse.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<WarehouseRespDto> successData;         // 성공한 창고 데이터들
    private List<WarehouseErrorDto> failData;           // 실패한 창고 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WarehouseErrorDto {
        private Integer warehouseCode;                  // 실패한 창고코드
        private String warehouseName;                   // 실패한 창고명  
        private String errorMessage;                    // 실패 사유
    }
}