package com.inc.sh.dto.orderLimitSet.respDto;

import com.inc.sh.entity.OrderLimitSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderLimitRespDto {
    
	private Integer limitCode;
    private String dayName; // 요일명
    private String limitStartTime; // 제한시작시간
    private String limitEndTime; // 제한종료시간
    
    // Entity를 DTO로 변환하는 팩토리 메서드
    public static OrderLimitRespDto fromEntity(OrderLimitSet entity) {
        return OrderLimitRespDto.builder()
        		.limitCode(entity.getLimitCode())
                .dayName(entity.getDayName())
                .limitStartTime(entity.getLimitStartTime())
                .limitEndTime(entity.getLimitEndTime())
                .build();
    }
}