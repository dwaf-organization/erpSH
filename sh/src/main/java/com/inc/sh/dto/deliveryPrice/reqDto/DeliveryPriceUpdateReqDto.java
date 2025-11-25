package com.inc.sh.dto.deliveryPrice.reqDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryPriceUpdateReqDto {
    
    @NotNull(message = "품목코드는 필수입니다")
    private Integer itemCode;
    
    @NotNull(message = "기본단가는 필수입니다")
    @Positive(message = "기본단가는 0보다 큰 값이어야 합니다")
    private Integer basePrice;
}