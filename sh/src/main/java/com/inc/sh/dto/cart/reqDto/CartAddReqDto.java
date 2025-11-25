package com.inc.sh.dto.cart.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartAddReqDto {
    
    private Integer customerUserCode;
    private Integer customerCode;
    private Integer itemCode;
    private Integer warehouseCode;
    private Integer orderQty;
}