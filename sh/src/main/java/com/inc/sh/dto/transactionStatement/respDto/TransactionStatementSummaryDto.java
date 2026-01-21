package com.inc.sh.dto.transactionStatement.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatementSummaryDto {
    
    private int totalOrderCount;       // 총 주문 건수
    private int totalSupplyAmt;        // 공급가 합계
    private int totalVatAmt;           // 부가세 합계
    private int totalAmt;              // 합계금액 합계
    private int totalTodayCollection;  // 당일수금 합계
    private int totalUncollectedBalance; // 미수잔액 합계
}