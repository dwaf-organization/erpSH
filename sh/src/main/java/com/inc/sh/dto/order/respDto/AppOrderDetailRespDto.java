package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderDetailRespDto {
    
    private OrderDto order;
    private List<OrderItemDto> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDto {
        private String orderNo;           // 주문번호
        private String orderDt;           // 주문일자 (yyyy-MM-dd)
        private String deliveryRequestDt; // 납기요청일 (yyyy-MM-dd)
        private String deliveryDt;        // 배송일자 (yyyy-MM-dd)
        private String deliveryStatus;    // 배송상태
        private String orderMessage;      // 주문메모
        private Integer totalAmt;         // 총금액
        private Integer totalItemCount;   // 총 품목 수
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private Integer itemCode;         // 품목코드
        private String itemName;          // 품목명
        private String specification;     // 규격
        private String unit;              // 단위
        private Integer orderQty;         // 주문수량
        private String priceType;         // 단가유형 ("납품싯가", "납품단가")
        private Integer orderUnitPrice;   // 주문단가
        private Integer totalAmt;         // 품목별 총금액
    }
}