package com.inc.sh.dto.returnRegistration.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnOrderItemRespDto {
    
    private Integer customerCode;           // 거래처코드
    private String customerName;            // 거래처명
    private Integer orderItemCode;       // 주문품목코드
    private String orderNo;              // 주문번호
    private Integer itemCode;            // 품목코드
    private Integer releaseWarehouseCode; // 출고창고코드
    private String warehouseName;        // 창고명
    private Integer distCenterCode;		 // 물류센터코드
    private String distCenterName;       // 물류센터명
    private String itemName;             // 품목명
    private String specification;        // 규격
    private String unit;                 // 단위
    private Integer priceType;           // 단가유형
    private Integer orderUnitPrice;      // 주문단가
    private Integer currentStockQty;     // 현재재고수량
    private Integer orderQty;            // 주문수량
    private String taxTarget;            // 과세대상
    private Integer taxableAmt;          // 과세금액
    private Integer taxFreeAmt;          // 면세금액
    private Integer supplyAmt;           // 공급가액
    private Integer vatAmt;              // 부가세액
    private Integer totalAmt;            // 합계금액
    private Integer totalQty;            // 총수량
    private Integer availableReturnQty;  // 반품가능수량
}