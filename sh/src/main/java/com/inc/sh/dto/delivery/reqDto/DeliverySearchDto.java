package com.inc.sh.dto.delivery.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverySearchDto {
    
    private String deliveryRequestDt;    // 납기일자
    private Integer customerCode;        // 거래처코드
    private String orderNo;              // 주문번호 (부분일치)
    private String deliveryStatus;       // 배송상태 (배송요청, 배송중, 배송완료)
    private Integer hqCode;              // 본사코드
}