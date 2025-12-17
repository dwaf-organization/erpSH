package com.inc.sh.dto.cart.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRespDto {
    
    private Integer customerCartCode;
    private Integer customerUserCode;
    private Integer customerCode;
    private Integer itemCode;
    private Integer virtualAccountCode;
    private Integer warehouseCode;
    private String itemName;
    private String specification;
    private String unit;
    private Integer priceType;
    private Integer orderUnitPrice;
    private Integer supplyPrice;        // 공급가액
    private Integer taxAmount;          // 부가세액
    private Integer taxableAmount;      // 과세액(과세 공급가액)
    private Integer dutyFreeAmount;     // 면세액
    private Integer totalAmount;        // 총액
    private Integer currentStockQty;
    private Integer orderQty;
    private String taxTarget;
    private String warehouseName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}