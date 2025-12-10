package com.inc.sh.dto.order.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchDto {
    
    private String orderDtStart;      // 주문일자 시작
    private String orderDtEnd;        // 주문일자 끝
    private String customerName;      // 거래처명 (부분일치)
    private String deliveryStatus;    // 배송상태 (배송요청, 배송중, 배송완료)
    private Integer hqCode;           // 본사코드
}