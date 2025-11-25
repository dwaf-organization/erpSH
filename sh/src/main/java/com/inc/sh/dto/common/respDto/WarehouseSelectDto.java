package com.inc.sh.dto.common.respDto;

import com.inc.sh.entity.Warehouse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseSelectDto {
    
    private Integer value;       // 창고코드 (value)
    private String label;        // 창고명 (옵션값)
    
    /**
     * Entity to DTO 변환 (셀렉트박스용)
     */
    public static WarehouseSelectDto fromEntity(Warehouse warehouse) {
        return WarehouseSelectDto.builder()
                .value(warehouse.getWarehouseCode())
                .label(warehouse.getWarehouseName())
                .build();
    }
}