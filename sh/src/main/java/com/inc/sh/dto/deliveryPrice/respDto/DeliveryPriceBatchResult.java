package com.inc.sh.dto.deliveryPrice.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceBatchResult {
    
    private int totalCount;                                 // 총 처리 건수
    private int successCount;                               // 성공 건수
    private int failCount;                                  // 실패 건수
    private List<DeliveryPriceUpdateRespDto> successData;   // 성공한 단가 수정 데이터들
    private List<DeliveryPriceErrorDto> failData;           // 실패한 단가 수정 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryPriceErrorDto {
        private Integer itemCode;                           // 실패한 품목코드
        private String itemName;                            // 실패한 품목명  
        private String errorMessage;                        // 실패 사유
    }
}