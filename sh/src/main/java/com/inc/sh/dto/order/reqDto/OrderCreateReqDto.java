package com.inc.sh.dto.order.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateReqDto {
    
    // 기본 주문 정보
    private Integer customerCode;
    private Integer customerUserCode;
    private String deliveryRequestDt;  // 납기요청일 (YYYYMMDD)
    private String orderMessage;       // 주문메시지
    
    // 주문 금액 정보
    private Integer taxableAmt;        // 과세금액
    private Integer taxFreeAmt;        // 면세금액
    private Integer supplyAmt;         // 공급가액
    private Integer vatAmt;            // 부가세
    private Integer totalAmt;          // 총금액
    private Integer totalQty;          // 총수량
    
    // 주문품목 리스트
    private List<OrderItemReqDto> orderItems;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemReqDto {
        private Integer itemCode;
        private Integer warehouseCode;
        private String itemName;
        private String specification;
        private String unit;
        private Integer priceType;
        private Integer orderUnitPrice;
        private Integer currentStockQty;
        private Integer orderQty;
        private String taxTarget;
        private Integer taxableAmt;
        private Integer taxFreeAmt;
        private Integer supplyAmt;
        private Integer vatAmt;
        private Integer totalAmt;
        private Integer totalQty;
    }
}