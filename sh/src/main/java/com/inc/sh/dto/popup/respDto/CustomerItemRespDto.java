package com.inc.sh.dto.popup.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerItemRespDto {
    
    // 기본 품목 정보
    private Integer itemCode;
    private String itemName;
    private String specification;
    private String purchaseUnit;
    private String vatType;
    private String vatDetail;
    private String origin;
    private Integer categoryCode;
    private String categoryName;
    
    // 창고 재고 정보
    private Integer warehouseCode;
    private String warehouseName;
    private Integer currentQuantity;        // 현재고량
    private Integer safeQuantity;           // 안전재고
    
    // 가격 정보 (거래처별 or 기본가격)
    private Integer basePrice;              // 기준단가
    private Integer supplyPrice;            // 공급가액
    private Integer taxAmount;              // 부가세액
    private Integer taxableAmount;          // 과세액
    private Integer dutyFreeAmount;         // 면세액
    private Integer totalAmount;            // 총액
    
    // 주문 관련 정보
    private Integer orderAvailableYn;       // 주문가능여부
    private Integer minOrderQty;            // 최소주문수량
    private Integer maxOrderQty;            // 최대주문수량
    private Integer deadlineDay;            // 마감일
    private String deadlineTime;            // 마감시간
}