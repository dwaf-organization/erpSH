package com.inc.sh.dto.orderLimitSet.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLimitDeleteReqDto {
    
    private List<Integer> limitCodes;  // 삭제할 주문제한설정코드 리스트
}