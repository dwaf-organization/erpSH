package com.inc.sh.dto.deliveryPrice.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceSearchDto {
    
    private Integer itemCode;     // 품목코드 (완전일치)
    private Integer categoryCode; // 분류코드 (완전일치)
    private Integer priceType;    // 단가유형 (1=납품싯가, 2=납품단가)
}