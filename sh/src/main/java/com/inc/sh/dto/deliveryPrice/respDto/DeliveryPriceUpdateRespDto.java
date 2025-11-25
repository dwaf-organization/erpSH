package com.inc.sh.dto.deliveryPrice.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceUpdateRespDto {
    
    private Integer itemCode;
    private Integer previousPrice; // 업데이트된 직전단가 (납품싯가의 경우)
    private Integer basePrice;     // 업데이트된 기본단가
    private String priceTypeDesc;  // 단가유형 설명
    private String message;        // 처리 내용 설명
}