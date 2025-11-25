package com.inc.sh.dto.customerBalance.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBalanceUpdateDto {
    
    private Integer customerCode;       // 거래처코드
    private Integer balanceAmt;         // 기초미수금 (덮어쓸 값)
}