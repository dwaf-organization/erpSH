package com.inc.sh.dto.monthlyInventoryStatus.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyInventoryStatusRespDto {
    
    private Integer warehouseCode;      // 창고코드
    private String warehouseName;       // 창고명
    private String categoryName;        // 분류명 (대분류명 또는 대분류명-중분류명)
    private Integer itemCode;           // 품목코드
    private String itemName;            // 품명
    private String specification;       // 규격
    private String unit;                // 단위
    
    // 전기이월
    private Integer openingQuantity;    // 전기이월수량
    private Integer openingAmount;      // 전기이월금액
    
    // 당기입고
    private Integer inQuantity;         // 당기입고수량
    private Integer inAmount;           // 당기입고금액
    
    // 당기출고
    private Integer outQuantity;        // 당기출고수량
    private Integer outAmount;          // 당기출고금액
    
    // 기말재고
    private Integer calQuantity;        // 계산수량
    private Integer actualQuantity;     // 실사수량
    private Integer actualUnitPrice;    // 단가
    private Integer actualAmount;       // 금액
    
    // 오차
    private Integer diffQuantity;       // 오차수량
    private Integer diffAmount;         // 오차금액
}