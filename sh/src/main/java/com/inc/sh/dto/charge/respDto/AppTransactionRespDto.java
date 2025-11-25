package com.inc.sh.dto.charge.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTransactionRespDto {
    
    private String transactionDate;     // 거래일자 (YYYY-MM-DD)
    private String transactionType;     // 거래유형 (입금/출금/조정/반품입금)
    private Integer amount;             // 거래금액
    private Integer balanceAfter;       // 거래후잔액
}