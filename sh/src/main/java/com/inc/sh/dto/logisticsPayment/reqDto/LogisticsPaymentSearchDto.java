package com.inc.sh.dto.logisticsPayment.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsPaymentSearchDto {
    
    private String orderNo;             // 주문번호 (부분검색)
    private Integer customerCode;       // 거래처코드 (완전일치)
    private String collectionDate;      // 회수기일 (YYYYMMDD, 완전일치, null 허용)
    private Integer hqCode;             // 본사코드
}