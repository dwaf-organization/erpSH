package com.inc.sh.dto.common.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleSelectDto {
    
    private Integer value;              // 차량코드
    private String label;               // 차량명
}