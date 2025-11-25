package com.inc.sh.dto.orderLimit.reqDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitUpdateReqDto {
    
    @NotNull(message = "품목코드는 필수입니다")
    private Integer itemCode;
    
    private List<CustomerLimitDto> customerLimits;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerLimitDto {
        
        @NotNull(message = "거래처코드는 필수입니다")
        private Integer customerCode;
        
        @NotNull(message = "제한여부는 필수입니다")
        private Integer isLimited; // 0=불가능, 1=가능
    }
}