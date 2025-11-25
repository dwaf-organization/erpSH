package com.inc.sh.dto.warehouse.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseListRespDto {
    
    private Integer warehouseCode;    // 창고코드
    private String warehouseName;     // 창고명
    private String distCenterName;    // 물류센터명
    
    /**
     * Object[] 배열에서 생성 (Repository 조인 결과)
     */
    public static WarehouseListRespDto of(Object[] result) {
        return WarehouseListRespDto.builder()
                .warehouseCode((Integer) result[0])
                .warehouseName((String) result[1])
                .distCenterName((String) result[2])
                .build();
    }
}