package com.inc.sh.dto.customerBalance.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBalanceRespDto {
    
    private Integer customerCode;       // 거래처코드
    private String customerName;        // 거래처명
    private String ownerName;           // 대표자
    private Integer creditLimit;        // 여신한도
    private Integer balanceAmt;         // 현재미수금
}