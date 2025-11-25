package com.inc.sh.dto.order.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSaveDto {
    
    private String orderNo;                     // 주문번호 (신규시 null)
    private Integer customerCode;               // 거래처코드
    private String orderDt;                     // 주문일자 (yyyyMMdd)
    private String deliveryRequestDt;           // 납기요청일 (yyyyMMdd)
    private String orderMessage;                // 주문메시지
    private Integer depositTypeCode;            // 입금유형 (0=후입금, 1=충전형)
    private String deliveryStatus;              // 배송상태 (배송요청, 배송중)
    private Integer deliveryAmt;                // 배송비
    private Integer distCenterCode;             // 물류센터코드
    
    // 배송중일 때만 필요한 필드들
    private Integer vehicleCode;                // 차량코드 (배송중일 때 필수)
    private String vehicleName;                 // 차량명 (자동 조회)
    private String deliveryDt;                  // 배송일자 (배송중일 때 order_dt와 동일)
    
    // 충전형 주문시 필요한 금액 정보
    private Integer totalAmt;                   // 총 주문금액 (잔액 체크용)
}