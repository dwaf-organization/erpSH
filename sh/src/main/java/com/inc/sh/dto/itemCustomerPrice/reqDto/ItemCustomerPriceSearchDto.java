package com.inc.sh.dto.itemCustomerPrice.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCustomerPriceSearchDto {
    
    private Integer categoryCode; // 분류코드 (완전일치)
    private String itemName;      // 품명 (부분일치)
}