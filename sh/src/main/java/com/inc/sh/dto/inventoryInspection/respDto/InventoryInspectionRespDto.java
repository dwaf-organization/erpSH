package com.inc.sh.dto.inventoryInspection.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryInspectionRespDto {
    
    private Integer closingCode;        // 마감코드
    private Integer itemCode;           // 품목코드
    private String itemName;            // 품명
    private String categoryName;        // 분류명
    private String specification;       // 규격
    private String unit;                // 단위
    private String transactionType;     // 최근거래유형
    private Integer safeQuantity;       // 안전재고량
    
    // 전기이월
    private Integer openingQuantity;    // 전기이월수량
    private Integer openingAmount;      // 전기이월금액
    
    // 당기입고
    private Integer inQuantity;         // 당기입고수량
    private Integer inAmount;           // 당기입고금액
    
    // 당기출고
    private Integer outQuantity;        // 당기출고수량
    private Integer outAmount;          // 당기출고금액
    
    // 계산
    private Integer calQuantity;        // 계산수량
    private Integer calAmount;          // 계산금액
    
    // 실사
    private Integer actualQuantity;     // 실사수량
    private Integer actualUnitPrice;    // 실사단가
    private Integer actualAmount;       // 실사금액
    
    // 오차
    private Integer diffQuantity;       // 오차수량
    private Integer diffAmount;         // 오차금액
}