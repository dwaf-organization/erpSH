package com.inc.sh.dto.itemCustomerPrice.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPriceRespDto {
    
    private Integer customerCode;    // 거래처코드
    private String brandName;        // 브랜드
    private String customerName;     // 거래처명
    private Integer basePrice;       // 기본단가 (품목의 기본단가)
    private Integer customerPrice;   // 거래처단가 (0일 수 있음)
    
    /**
     * Customer Entity와 Item 기본단가, 거래처단가로 생성
     */
    public static CustomerPriceRespDto of(Integer customerCode, String brandName, String customerName, 
                                          Integer basePrice, Integer customerPrice) {
        return new CustomerPriceRespDto(customerCode, brandName, customerName, basePrice, 
                                        customerPrice != null ? customerPrice : 0);
    }
}