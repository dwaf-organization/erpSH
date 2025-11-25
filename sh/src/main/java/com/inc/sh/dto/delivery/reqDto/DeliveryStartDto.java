package com.inc.sh.dto.delivery.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryStartDto {
    
    private List<DeliveryOrderDto> orders;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryOrderDto {
        private String orderNo;           // 주문번호
        private Integer vehicleCode;      // 차량코드
        private String vehicleName;       // 차량명
        private Integer deliveryAmt;      // 배송금액
        private String deliveryDt;        // 배송일자
    }
}