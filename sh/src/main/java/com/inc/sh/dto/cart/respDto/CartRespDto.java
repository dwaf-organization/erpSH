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
    private Integer currentStockQty;
    private Integer orderQty;
    private String taxTarget;
    private String warehouseName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}