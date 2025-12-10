package com.inc.sh.dto.deliveryHoliday.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidayDeleteReqDto {
    
    private List<Integer> deliveryHolidayCodes;  // 삭제할 배송휴일코드 배열
}