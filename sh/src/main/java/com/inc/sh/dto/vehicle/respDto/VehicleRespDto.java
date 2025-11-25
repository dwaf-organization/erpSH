package com.inc.sh.dto.vehicle.respDto;

import com.inc.sh.entity.Vehicle;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRespDto {
    
    private Integer vehicleCode;
    private String vehicleName;
    private String vehicleType;     // 차종
    private String capacitySpec;    // 적재량/스펙
    private String category;        // 구분
    
    /**
     * Entity to DTO 변환
     */
    public static VehicleRespDto fromEntity(Vehicle vehicle) {
        return VehicleRespDto.builder()
                .vehicleCode(vehicle.getVehicleCode())
                .vehicleName(vehicle.getVehicleName())
                .vehicleType(vehicle.getVehicleType())
                .capacitySpec(vehicle.getCapacitySpec())
                .category(vehicle.getCategory())
                .build();
    }
}