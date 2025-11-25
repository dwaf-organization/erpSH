package com.inc.sh.dto.headquarter.respDto;

import com.inc.sh.entity.Headquarter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderConfigRespDto {

	private Integer hqCode;
    private Integer logisticsType;
    private String priceDisplayType;
    
    public static OrderConfigRespDto fromEntity(Headquarter entity) { 
        return OrderConfigRespDto.builder()
                .hqCode(entity.getHqCode())
                .logisticsType(entity.getLogisticsType())
                .priceDisplayType(entity.getPriceDisplayType())
                .build();
    }
}
