package com.inc.sh.dto.warehouseTransfer.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransferListSearchDto {
    
    private String startYm;             // 시작년월 (YYYYMM)
    private String endYm;               // 종료년월 (YYYYMM)
    private Integer fromWarehouseCode;  // 출고창고코드 (null이면 전체)
    private Integer toWarehouseCode;    // 입고창고코드 (null이면 전체)
}