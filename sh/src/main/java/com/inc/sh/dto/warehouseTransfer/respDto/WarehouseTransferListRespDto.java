package com.inc.sh.dto.warehouseTransfer.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferListRespDto {
    
    private String transferCode;        // 이송번호
    private String transferDate;        // 이송일자 (YYYY-MM-DD)
    private Integer fromWarehouseCode;  // 출고창고코드
    private String fromWarehouseName;   // 출고창고명
    private Integer toWarehouseCode;    // 입고창고코드
    private String toWarehouseName;     // 입고창고명
    private String note;                // 비고
}