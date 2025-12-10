package com.inc.sh.dto.customerLedger.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLedgerSummarySearchDto {
    
    private String deliveryRequestDtStart;  // 납기요청일 시작
    private String deliveryRequestDtEnd;    // 납기요청일 끝
    private Integer itemCode;               // 품목코드
    private Integer brandCode;              // 브랜드코드
    private Integer customerCode;           // 거래처코드
    private String orderStatus;             // 주문상태 (전체, 주문, 배송, 반품)
    private Integer hqCode;                 // 본사코드
}