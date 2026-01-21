package com.inc.sh.dto.transactionStatement.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatementRespDto {
    
    private String orderNo;             // 주문번호
    private String customerName;        // 거래처명
    private String bizNum;              // 거래처사업자번호
    private Integer supplyAmt;          // 공급가
    private Integer vatAmt;             // 부가세
    private Integer totalAmt;           // 합계금액
    private Integer todayCollection;    // 당일수금
    private Integer uncollectedBalance; // 미수잔액
    private String origin;              // 원산지 (Order 테이블에 없어서 일단 추가)
    private String depositType;         // 입금유형 (충전형/후입금) - 참고용
}