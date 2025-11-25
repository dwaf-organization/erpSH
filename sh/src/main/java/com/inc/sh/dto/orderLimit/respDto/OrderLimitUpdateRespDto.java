package com.inc.sh.dto.orderLimit.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitUpdateRespDto {
    
    private Integer itemCode;
    private Integer updatedOrderAvailableYn; // 업데이트된 주문가능여부
    private Integer processedCount;          // 처리된 거래처 수
    private List<ProcessResult> processResults;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessResult {
        private Integer customerCode;
        private String action;    // "ADDED", "REMOVED", "NO_CHANGE"
        private Integer isLimited; // 0=불가능, 1=가능
        private String message;
    }
}