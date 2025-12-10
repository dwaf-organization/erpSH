package com.inc.sh.dto.item.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemBatchResult {
    
    private int totalCount;                     // 총 처리 건수
    private int successCount;                   // 성공 건수
    private int failCount;                      // 실패 건수
    private List<ItemRespDto> successData;      // 성공한 품목 데이터들
    private List<ItemErrorDto> failData;       // 실패한 품목 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemErrorDto {
        private Integer itemCode;               // 실패한 품목코드
        private String itemName;                // 실패한 품목명  
        private String errorMessage;            // 실패 사유
    }
}