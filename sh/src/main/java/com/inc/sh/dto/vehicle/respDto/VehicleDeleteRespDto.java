package com.inc.sh.dto.vehicle.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleDeleteRespDto {
    
    private Integer vehicleCode;
    private List<String> activeOrderNos; // 배송중인 주문번호들
    private String message;
}