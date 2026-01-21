package com.inc.sh.dto.transactionStatement.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatementSearchDto {
    
    private String deliveryRequestDt;   // 납기일자 (YYYYMMDD) - 필수
    private Integer customerCode;       // 거래처코드 (null 가능 - 전체)
    private Integer brandCode;          // 브랜드코드 (null 가능 - 전체)
    private Integer hqCode;             // 본사코드 (필수)
}