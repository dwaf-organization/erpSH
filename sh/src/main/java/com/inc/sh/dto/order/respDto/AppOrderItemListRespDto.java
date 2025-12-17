package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderItemListRespDto {
    
    private Integer itemCode;
    private String itemName;
    private String specification;
    private String unit;
    private String vatType;
    private String vatDetail;
    private Integer categoryCode;
    private String origin;
    private Integer priceType;
    private Integer customerPrice;
    private Integer supplyPrice;
    private Integer taxAmount;
    private Integer taxableAmount;
    private Integer dutyFreeAmount;
    private Integer totalAmt;
    private Integer orderAvailableYn;
    private Integer minOrderQty;
    private Integer maxOrderQty;
    private Integer deadlineDay;
    private String deadlineTime;
    private Integer currentQuantity;
    private Integer warehouseCode;
    private Boolean isWishlist;
    private Integer customerWishlistCode;
}