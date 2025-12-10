package com.inc.sh.dto.order.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSaveReqDto {
    
    private List<OrderSaveItemDto> orders;  // 주문 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderSaveItemDto {
        private String orderNo;                     // null=CREATE, 값=UPDATE
        private Integer hqCode;                     // 본사코드 (필수)
        private Integer customerCode;               // 거래처코드 (필수)
        private String orderDt;                     // 주문일자 (yyyyMMdd) (필수)
        private String deliveryRequestDt;           // 납기요청일 (yyyyMMdd) (필수)
        private String orderMessage;                // 주문메시지
        private Integer depositTypeCode;            // 입금유형 (0=후입금, 1=충전형)
        private String deliveryStatus;              // 배송상태 (배송요청, 배송중)
        private Integer deliveryAmt;                // 배송비
        private Integer distCenterCode;             // 물류센터코드
        
        // 배송중일 때만 필요한 필드들
        private Integer vehicleCode;                // 차량코드 (배송중일 때 필수)
        private String vehicleName;                 // 차량명 (자동 조회)
        private String deliveryDt;                  // 배송일자 (배송중일 때 order_dt와 동일)
        
    }
}