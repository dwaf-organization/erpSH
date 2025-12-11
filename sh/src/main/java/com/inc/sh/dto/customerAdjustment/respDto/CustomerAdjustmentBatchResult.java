package com.inc.sh.dto.customerAdjustment.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentBatchResult {
    
    private int totalCount;                            // 총 처리 건수
    private int successCount;                          // 성공 건수
    private int failureCount;                          // 실패 건수
    
    private List<CustomerAdjustmentSuccessResult> successList;    // 성공 목록
    private List<CustomerAdjustmentFailureResult> failureList;    // 실패 목록
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerAdjustmentSuccessResult {
        private Integer transactionCode;               // 거래내역코드
        private Integer customerCode;                  // 거래처코드
        private String customerName;                   // 거래처명
        private Integer adjustmentAmount;              // 조정금액
        private Integer finalBalance;                  // 최종잔액
        private String message;                        // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerAdjustmentFailureResult {
        private Integer transactionCode;               // 거래내역코드 (null 가능)
        private Integer customerCode;                  // 거래처코드 (null 가능)
        private String reason;                         // 실패 원인
    }
}