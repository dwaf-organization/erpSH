package com.inc.sh.dto.itemCustomerPrice.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerWithBrandDto {
    
    private Integer customerCode;
    private String customerName;
    private String brandName;
    
    /**
     * Object[] 배열에서 생성 (Repository 조인 결과)
     */
    public static CustomerWithBrandDto of(Object[] result) {
        return CustomerWithBrandDto.builder()
                .customerCode((Integer) result[0])
                .customerName((String) result[1])
                .brandName((String) result[2])
                .build();
    }
}