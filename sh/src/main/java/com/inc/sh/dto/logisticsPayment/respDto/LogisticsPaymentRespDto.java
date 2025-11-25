package com.inc.sh.dto.logisticsPayment.respDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogisticsPaymentRespDto {
    
    private String orderNo;             // 주문번호
    private Integer customerCode;       // 거래처코드
    private String customerName;        // 거래처명
    private String orderDate;           // 주문일자 (YYYYMMDD)
    private String collectionDueDate;   // 회수기일 (주문일자 + 회수일)
    private String paymentDate;         // 납부일자 (결제일자)
    private Integer supplyAmount;       // 공급가
    private Integer vatAmount;          // 부가세
    private Integer totalAmount;        // 합계금액
    
    // 추가 정보
    private Integer collectionDay;      // 회수일 (거래처별)
    private String paymentStatus;       // 결제상태
}