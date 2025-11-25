package com.inc.sh.dto.deliveryHoliday.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidayRespDto {
    
    private Integer deliveryHolidayCode;
    private String brandName;       // 브랜드명
    private String holidayDt;       // 휴일
    private String weekday;         // 요일
    private String holidayName;     // 휴일명
    
    /**
     * Object[] 배열에서 생성 (Repository 조인 결과)
     */
    public static DeliveryHolidayRespDto of(Object[] result) {
        return DeliveryHolidayRespDto.builder()
                .deliveryHolidayCode((Integer) result[0])
                .brandName((String) result[1])
                .holidayDt((String) result[2])
                .weekday((String) result[3])
                .holidayName((String) result[4])
                .build();
    }
}