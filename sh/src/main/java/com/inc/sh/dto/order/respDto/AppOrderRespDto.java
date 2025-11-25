package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderRespDto {
    
    private String orderNo;
    private Integer totalAmt;
    private String message;
}