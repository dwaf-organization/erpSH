package com.inc.sh.dto.warehouseTransfer.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferProcessDto {
    
    private String transferDate;        // 이송일자 (YYYYMMDD)
    private Integer fromWarehouseCode;  // 출고창고코드
    private Integer toWarehouseCode;    // 입고창고코드
    private String note;                // 비고
    private List<TransferItemDto> items; // 이송품목 리스트
}