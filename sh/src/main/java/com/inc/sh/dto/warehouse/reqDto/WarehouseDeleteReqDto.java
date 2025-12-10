package com.inc.sh.dto.warehouse.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDeleteReqDto {
    
    private List<Integer> warehouseCodes;  // 삭제할 창고코드 배열
}