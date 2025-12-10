package com.inc.sh.dto.returnManagement.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnSaveReqDto {
    
    private List<ReturnSaveItemDto> returns;  // 반품 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnSaveItemDto {
        private String returnNo;                    // null=CREATE, 값=UPDATE
        private Integer returnCustomerCode;         // 거래처코드 (필수)
        private Integer itemCode;                   // 품목코드 (필수)
        private Integer receiveWarehouseCode;       // 입고창고코드
        private String returnCustomerName;          // 거래처명
        private String returnRequestDt;             // 반품요청일자 (YYYYMMDD) (필수)
        private String itemName;                    // 품명
        private String specification;               // 규격
        private String unit;                        // 단위
        private Integer qty;                        // 수량 (필수)
        private String priceType;                   // 단가유형
        private Integer unitPrice;                  // 단가
        private BigDecimal supplyPrice;             // 공급가액
        private BigDecimal vat;                     // 부가세
        private BigDecimal totalAmount;             // 합계금액
        private String returnMessage;               // 반품사유
        private String note;                        // 비고
        private String progressStatus;              // 진행상태
        private String warehouseName;               // 창고명
        private Integer orderItemCode;              // 주문품목코드 (필수)
        private String orderNo;                     // 주문번호 (필수)
    }
}