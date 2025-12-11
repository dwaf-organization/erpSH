package com.inc.sh.dto.order.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentBatchResult {
    
    private int totalCount;                                       // 총 처리 건수
    private int successCount;                                     // 성공 건수
    private int failureCount;                                     // 실패 건수
    
    private List<OrderPaymentSuccessResult> successList;          // 성공 목록
    private List<OrderPaymentFailureResult> failureList;          // 실패 목록
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderPaymentSuccessResult {
        private String orderNo;                                   // 주문번호
        private String customerName;                              // 거래처명
        private Integer totalAmt;                                 // 총금액
        private String previousStatus;                            // 이전 결제상태
        private String currentStatus;                             // 현재 결제상태 (결제완료)
        private String message;                                   // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderPaymentFailureResult {
        private String orderNo;                                   // 주문번호
        private String reason;                                    // 실패 원인
    }
}