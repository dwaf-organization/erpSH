package com.inc.sh.dto.orderLimit.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLimitRespDto {
    
    private Integer customerCode;    // 거래처코드
    private String customerName;     // 거래처명
    private String brandName;        // 브랜드명
    private Integer isLimited;       // 불가여부 (0=불가능, 1=가능)
    
    /**
     * Object[] 배열에서 생성 (Repository 조인 결과)
     */
    public static CustomerLimitRespDto of(Object[] result, boolean isLimited) {
        return CustomerLimitRespDto.builder()
                .customerCode((Integer) result[0])
                .customerName((String) result[1])
                .brandName((String) result[2])
                .isLimited(isLimited ? 0 : 1) // true(테이블에 있음) = 0(불가능), false(테이블에 없음) = 1(가능)
                .build();
    }
}