package com.inc.sh.dto.warehouse.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseSearchDto {
    
    private Integer warehouseCode;   // 창고코드 (완전일치)
    private Integer distCenterCode;  // 물류센터코드 (완전일치)
    private Integer hqCode;          // 본사코드
}