package com.inc.sh.dto.vehicle.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDeleteReqDto {
    
    private List<Integer> vehicleCodes;  // 삭제할 차량코드 배열
}