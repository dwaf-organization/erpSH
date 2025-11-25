package com.inc.sh.dto.deliveryHoliday.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidaySaveRespDto {
    
    private Integer deliveryHolidayCode;
    private String holidayType;     // 휴일타입 (기본휴일, 정기휴일)
}