package com.inc.sh.dto.customerBalance.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBalanceUpdateDto {
    
    private List<CustomerBalanceItemDto> customers;  // 거래처 배열
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerBalanceItemDto {
        private Integer customerCode;       // 거래처코드 (필수)
        private Integer balanceAmt;         // 기초미수금 (덮어쓸 값) (필수)
    }
}