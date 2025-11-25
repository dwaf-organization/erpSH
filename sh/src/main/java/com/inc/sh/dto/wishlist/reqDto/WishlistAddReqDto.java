package com.inc.sh.dto.wishlist.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistAddReqDto {
    
    private Integer customerCode;
    private Integer customerUserCode;
    private Integer itemCode;
    private Integer currentStockQty;
    private Integer warehouseCode;
}