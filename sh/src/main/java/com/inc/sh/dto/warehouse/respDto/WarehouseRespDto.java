package com.inc.sh.dto.warehouse.respDto;

import com.inc.sh.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseRespDto {
    
    private Integer warehouseCode;
    private Integer distCenterCode;
    private Integer hqCode;
    private String warehouseName;
    private String zipCode;
    private String addr;
    private String telNum;
    private String managerName;
    private String managerContact;
    private Integer useYn;
    
    /**
     * Entity를 DTO로 변환
     */
    public static WarehouseRespDto fromEntity(Warehouse warehouse) {
        return WarehouseRespDto.builder()
                .warehouseCode(warehouse.getWarehouseCode())
                .distCenterCode(warehouse.getDistCenterCode())
                .hqCode(warehouse.getHqCode())
                .warehouseName(warehouse.getWarehouseName())
                .zipCode(warehouse.getZipCode())
                .addr(warehouse.getAddr())
                .telNum(warehouse.getTelNum())
                .managerName(warehouse.getManagerName())
                .managerContact(warehouse.getManagerContact())
                .useYn(warehouse.getUseYn())
                .build();
    }
}