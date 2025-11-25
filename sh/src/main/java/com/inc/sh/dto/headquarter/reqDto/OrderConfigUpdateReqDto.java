package com.inc.sh.dto.headquarter.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfigUpdateReqDto {

    private Integer hqCode;
    private Integer logisticsType;
    private String priceDisplayType;
}