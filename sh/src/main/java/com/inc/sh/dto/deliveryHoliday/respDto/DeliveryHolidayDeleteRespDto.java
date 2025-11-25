package com.inc.sh.dto.deliveryHoliday.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidayDeleteRespDto {
    
    private Integer deliveryHolidayCode;
    private String message;
}