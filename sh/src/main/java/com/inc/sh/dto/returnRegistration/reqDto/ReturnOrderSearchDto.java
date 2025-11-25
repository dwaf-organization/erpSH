package com.inc.sh.dto.returnRegistration.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnOrderSearchDto {
    
    private Integer customerCode;       // 거래처코드 (완전일치, null 가능)
    private String orderNo;             // 주문번호 (완전일치, null 가능)
}