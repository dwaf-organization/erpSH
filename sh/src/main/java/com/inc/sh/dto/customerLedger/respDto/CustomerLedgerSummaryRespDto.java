package com.inc.sh.dto.customerLedger.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerLedgerSummaryRespDto {
    
    private Integer customerCode;      // 거래처코드
    private String customerName;       // 거래처명
    private String brandName;          // 브랜드명
    private String telNum;             // 연락처
    private String orderType;          // 주문구분 (주문, 배송, 반품)
    private Integer totalQty;          // 주문량
    private Integer taxFreeAmt;        // 면세공급가
    private Integer taxableAmt;        // 과세공급가
    private Integer supplyAmt;         // 공급가
    private Integer vatAmt;            // 부가세
    private Integer totalAmt;          // 합계금액
    
    // 물류배송 항목
    private Integer deliveryQty;       // 배송량
    private Integer deliverySupplyAmt; // 배송 공급가
    private Integer deliveryVatAmt;    // 배송 부가세
    private Integer deliveryTotalAmt;  // 배송 합계금액
}