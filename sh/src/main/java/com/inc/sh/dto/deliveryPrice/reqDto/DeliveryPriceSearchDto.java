package com.inc.sh.dto.deliveryPrice.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceSearchDto {
    
    private Integer itemCode;     // 품목코드
    private Integer categoryCode; // 분류코드
    private Integer priceType;    // 단가유형 (1=납품단가, 2=납품싯가)
    private Integer hqCode;       // 본사코드
}