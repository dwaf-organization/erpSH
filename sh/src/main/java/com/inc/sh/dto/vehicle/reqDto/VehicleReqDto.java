package com.inc.sh.dto.vehicle.reqDto;

import com.inc.sh.entity.Vehicle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer vehicleCode;
    
    @NotBlank(message = "차량명은 필수입니다")
    private String vehicleName;
    
    @NotBlank(message = "구분은 필수입니다")
    private String category;        // 냉장, 냉동, 상온
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    private String vehicleType;     // 차종
    private String capacitySpec;    // 적재량/스펙
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public Vehicle toEntity() {
        return Vehicle.builder()
                .vehicleName(this.vehicleName)
                .category(this.category)
                .hqCode(this.hqCode)
                .vehicleType(this.vehicleType)
                .capacitySpec(this.capacitySpec)
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(Vehicle vehicle) {
        vehicle.setVehicleName(this.vehicleName);
        vehicle.setCategory(this.category);
        vehicle.setHqCode(this.hqCode);
        vehicle.setVehicleType(this.vehicleType);
        vehicle.setCapacitySpec(this.capacitySpec);
    }
}