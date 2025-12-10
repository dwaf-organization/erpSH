package com.inc.sh.dto.order.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDeleteReqDto {
    
    private List<String> orderNos;  // 삭제할 주문번호 배열
}