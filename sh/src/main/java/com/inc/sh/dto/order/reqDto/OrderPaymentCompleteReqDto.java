package com.inc.sh.dto.order.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaymentCompleteReqDto {
    
    private List<String> orderNos;    // 결제완료 처리할 주문번호 배열
}