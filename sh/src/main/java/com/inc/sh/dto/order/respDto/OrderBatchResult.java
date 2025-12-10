package com.inc.sh.dto.order.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<OrderRespDto> successData;             // 성공한 주문 데이터들
    private List<OrderErrorDto> failData;               // 실패한 주문 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderErrorDto {
        private String orderNo;                         // 실패한 주문번호
        private String customerName;                    // 실패한 거래처명  
        private String errorMessage;                    // 실패 사유
    }
}