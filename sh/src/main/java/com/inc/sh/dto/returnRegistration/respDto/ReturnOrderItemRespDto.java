package com.inc.sh.dto.returnRegistration.respDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnOrderItemRespDto {
    
    // 기본 정보
    private Integer customerCode;           // 거래처코드
    private String customerName;            // 거래처명 (JOIN)
    private Integer orderItemCode;          // 주문품목코드
    private String orderNo;                 // 주문번호
    
    // 품목 정보
    private Integer itemCode;               // 품목코드
    private String itemName;                // 품명
    private String specification;           // 규격
    private String unit;                    // 단위
    private String priceType;               // 단가유형
    
    // 주문 정보
    private Integer orderPrice;             // 주문단가
    private Integer orderQuantity;          // 주문수량
    private String taxTarget;               // 과세여부 (과세/면세)
    
    // 창고 정보
    private Integer warehouseCode;          // 창고코드
    private String warehouseName;           // 창고명 (JOIN)
    
    private Integer distCenterCode;
    private String distCenterName;
    
    // 금액 정보
    private Integer taxableAmount;          // 과세금액
    private Integer taxFreeAmount;          // 면세금액
    private Integer supplyAmount;           // 공급가액
    private Integer vatAmount;              // 부가세
    private Integer totalAmount;            // 합계금액
    
    private Integer availableReturnQty;     // 주문가능수량
}