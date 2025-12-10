package com.inc.sh.dto.deliveryHoliday.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidaySearchDto {
    
    private Integer brandCode; // 브랜드코드 (완전일치)
    private Integer hqCode;    // 본사코드
}