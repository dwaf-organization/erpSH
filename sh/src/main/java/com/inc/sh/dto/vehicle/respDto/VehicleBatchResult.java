package com.inc.sh.dto.vehicle.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<VehicleRespDto> successData;           // 성공한 차량 데이터들
    private List<VehicleErrorDto> failData;             // 실패한 차량 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleErrorDto {
        private Integer vehicleCode;                    // 실패한 차량코드
        private String vehicleName;                     // 실패한 차량명  
        private String errorMessage;                    // 실패 사유
    }
}