package com.inc.sh.dto.warehouseTransfer.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferItemDto {
    
    private String transferDate;        // 이송일자 (YYYYMMDD)
    private Integer fromWarehouseCode;  // 출고창고코드
    private Integer toWarehouseCode;    // 입고창고코드
    private Integer itemCode;           // 품목코드
    private Integer quantity;           // 이송수량
    private Integer unitPrice;          // 단가
    private Integer amount;             // 금액
    private String note;                // 비고
}