package com.inc.sh.dto.warehouse.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseDeleteRespDto {
    
    private Integer warehouseCode;
    private Long itemCount;     // 창고에 있는 품목 개수
    private String message;
}