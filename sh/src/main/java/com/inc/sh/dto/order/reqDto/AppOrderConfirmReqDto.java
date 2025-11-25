package com.inc.sh.dto.order.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderConfirmReqDto {
    
    private String orderNo;        // 주문번호
    private Integer customerCode;  // 거래처코드
}