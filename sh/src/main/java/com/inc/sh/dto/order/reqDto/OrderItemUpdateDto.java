package com.inc.sh.dto.order.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemUpdateDto {
    
    private String orderNo;                    // 주문번호
    private List<OrderItemSaveDto> orderItems; // 주문품목 목록
    
    // Order 테이블 업데이트용 금액 정보
    private Integer taxableAmt;    // 과세금액
    private Integer taxFreeAmt;    // 면세금액
    private Integer supplyAmt;     // 공급가액
    private Integer vatAmt;        // 부가세
    private Integer totalAmt;      // 총금액
    private Integer totalQty;      // 총수량
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemSaveDto {
        private Integer orderItemCode;       // 주문품목코드 (수정시)
        private Integer itemCode;            // 품목코드
        private Integer releaseWarehouseCode; // 창고코드
        private String itemName;             // 품명
        private String specification;        // 규격
        private String unit;                 // 단위
        private Integer priceType;           // 단가유형
        private Integer orderUnitPrice;      // 주문단가
        private Integer currentStockQty;     // 현재고량
        private Integer orderQty;            // 주문수량
        private String taxTarget;            // 과세대상
        private String warehouseName;        // 출고창고명
        private Integer taxableAmt;          // 과세금액
        private Integer taxFreeAmt;          // 면세금액
        private Integer supplyAmt;           // 공급가액
        private Integer vatAmt;              // 부가세
        private Integer totalAmt;            // 합계금액
        private Integer totalQty;            // 합계수량
        private String description;          // 적요
    }
}