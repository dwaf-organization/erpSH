package com.inc.sh.dto.order.respDto;

import com.inc.sh.entity.OrderItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRespDto {
    
    private Integer orderItemCode;
    private String orderNo;
    private Integer itemCode;
    private Integer releaseWarehouseCode;
    private String itemName;
    private String specification;
    private String unit;
    private Integer priceType;
    private Integer orderUnitPrice;
    private Integer currentStockQty;
    private Integer orderQty;
    private String taxTarget;
    private String warehouseName;
    private Integer taxableAmt;
    private Integer taxFreeAmt;
    private Integer supplyAmt;
    private Integer vatAmt;
    private Integer totalAmt;
    private Integer totalQty;
    private String description;
    
    /**
     * Entity to DTO 변환
     */
    public static OrderItemRespDto fromEntity(OrderItem orderItem) {
        return OrderItemRespDto.builder()
                .orderItemCode(orderItem.getOrderItemCode())
                .orderNo(orderItem.getOrderNo())
                .itemCode(orderItem.getItemCode())
                .releaseWarehouseCode(orderItem.getReleaseWarehouseCode())
                .itemName(orderItem.getItemName())
                .specification(orderItem.getSpecification())
                .unit(orderItem.getUnit())
                .priceType(orderItem.getPriceType())
                .orderUnitPrice(orderItem.getOrderUnitPrice())
                .currentStockQty(orderItem.getCurrentStockQty())
                .orderQty(orderItem.getOrderQty())
                .taxTarget(orderItem.getTaxTarget())
                .warehouseName(orderItem.getWarehouseName())
                .taxableAmt(orderItem.getTaxableAmt())
                .taxFreeAmt(orderItem.getTaxFreeAmt())
                .supplyAmt(orderItem.getSupplyAmt())
                .vatAmt(orderItem.getVatAmt())
                .totalAmt(orderItem.getTotalAmt())
                .totalQty(orderItem.getTotalQty())
                .description(orderItem.getDescription())
                .build();
    }
}