package com.inc.sh.dto.orderLimitSet.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLimitSaveReqDto {

    private Integer limitCode; // null이면 CREATE, 값이 있으면 UPDATE (핵심 포인트)
    private Integer brandCode;
    private Integer hqCode; // hq_code 필드가 테이블에 있으므로 추가
    private String dayName;
    private String limitStartTime;
    private String limitEndTime;
}