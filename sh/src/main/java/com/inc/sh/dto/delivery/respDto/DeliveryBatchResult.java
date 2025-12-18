package com.inc.sh.dto.delivery.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryBatchResult {
    
    private Integer totalCount;     // 총 처리 건수
    private Integer successCount;   // 성공 건수
    private Integer failCount;      // 실패 건수
    private List<DeliverySuccessResult> successData;  // 성공 데이터
    private List<DeliveryFailureResult> failData;     // 실패 데이터
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliverySuccessResult {
        private String orderNo;         // 주문번호
        private String customerName;    // 거래처명
        private String deliveryStatus;  // 변경된 배송상태
        private String message;         // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryFailureResult {
        private String orderNo;         // 주문번호
        private String reason;          // 실패 사유
    }
}