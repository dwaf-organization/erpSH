package com.inc.sh.dto.deliveryPrice.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceUpdateReqDto {
    
    private List<DeliveryPriceItemDto> items;  // 납품단가 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryPriceItemDto {
        private Integer itemCode;       // 품목코드 (필수)
        private Integer basePrice;      // 새로운 기본단가 (필수)
    }
}