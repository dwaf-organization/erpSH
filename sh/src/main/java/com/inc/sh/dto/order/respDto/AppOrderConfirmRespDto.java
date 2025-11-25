package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderConfirmRespDto {
    
    private String orderNo;         // 주문번호
    private String deliveryStatus;  // 배송상태 ("배송완료")
    private String deliveryDt;      // 배송일자 (yyyy-MM-dd)
}