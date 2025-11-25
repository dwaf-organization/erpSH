package com.inc.sh.dto.order.respDto;

import com.inc.sh.entity.Order;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRespDto {
    
    private String orderNo;
    private Integer customerCode;
    private String customerName;
    private String bizNum;
    private String zipCode;
    private String addr;
    private String orderDt;
    private String deliveryRequestDt;
    private Integer depositTypeCode;
    private String deliveryStatus;
    private Integer deliveryAmt;
    private String distCenterName;
    private String orderMessage;
    private Integer taxableAmt;
    private Integer taxFreeAmt;
    private Integer supplyAmt;
    private Integer vatAmt;
    private Integer totalAmt;
    private Integer totalQty;
    
    /**
     * Entity to DTO 변환 (조회용)
     */
    public static OrderRespDto fromEntity(Order order) {
        return OrderRespDto.builder()
                .orderNo(order.getOrderNo())
                .customerCode(order.getCustomerCode())
                .customerName(order.getCustomerName())
                .bizNum(order.getBizNum())
                .addr(order.getAddr())
                .orderDt(order.getOrderDt())
                .deliveryRequestDt(order.getDeliveryRequestDt())
                .depositTypeCode(order.getDepositTypeCode())
                .deliveryStatus(order.getDeliveryStatus())
                .deliveryAmt(order.getDeliveryAmt())
                .distCenterName(order.getDistCenterName())
                .orderMessage(order.getOrderMessage())
                .taxableAmt(order.getTaxableAmt())
                .taxFreeAmt(order.getTaxFreeAmt())
                .supplyAmt(order.getSupplyAmt())
                .vatAmt(order.getVatAmt())
                .totalAmt(order.getTotalAmt())
                .totalQty(order.getTotalQty())
                .build();
    }
}