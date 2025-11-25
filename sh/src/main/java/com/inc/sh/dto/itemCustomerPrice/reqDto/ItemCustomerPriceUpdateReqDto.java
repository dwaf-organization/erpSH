package com.inc.sh.dto.itemCustomerPrice.reqDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCustomerPriceUpdateReqDto {
    
    @NotNull(message = "품목코드는 필수입니다")
    private Integer itemCode;
    
    private List<CustomerPriceDto> customerPrices;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerPriceDto {
        
        @NotNull(message = "거래처코드는 필수입니다")
        private Integer customerCode;
        
        @NotNull(message = "거래처단가는 필수입니다")
        @PositiveOrZero(message = "거래처단가는 0 이상이어야 합니다")
        private Integer customerPrice;
        
        private String startDt;     // 적용시작일자 (YYYYMMDD)
        private String description; // 적요
    }
}