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
public class VehicleSaveReqDto {
    
    private List<VehicleItemDto> vehicles;  // 차량 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleItemDto {
        private Integer vehicleCode;            // null=CREATE, 값=UPDATE
        private String vehicleName;             // 차량명 (필수)
        private String category;                // 구분 (냉장, 냉동, 상온) (필수)
        private Integer hqCode;                 // 본사코드 (필수)
        private String vehicleType;             // 차종
        private String capacitySpec;            // 적재량/스펙
    }
}