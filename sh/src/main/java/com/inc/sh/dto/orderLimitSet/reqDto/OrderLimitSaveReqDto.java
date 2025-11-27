package com.inc.sh.dto.orderLimitSet.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLimitSaveReqDto {
    
    private Integer brandCode;  // 공통 브랜드코드
    private Integer hqCode;     // 공통 본사코드
    private List<OrderLimitItemDto> limits;  // 다중 저장용 리스트
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLimitItemDto {
        private Integer limitCode;      // null이면 CREATE, 값이 있으면 UPDATE
        private String dayName;         // 요일명 (월,화,수,목,금,토,일)
        private String limitStartTime;  // 제한시작시간 (HH:mm)
        private String limitEndTime;    // 제한종료시간 (HH:mm)
    }
}