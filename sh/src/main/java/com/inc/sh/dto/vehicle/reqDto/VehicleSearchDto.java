package com.inc.sh.dto.vehicle.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSearchDto {
    
    private Integer vehicleCode; // 차량코드 (완전일치)
    private String category;     // 구분 (냉장, 냉동, 상온)
}
