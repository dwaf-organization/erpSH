package com.inc.sh.dto.delivery.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCancelDto {
    
    private List<String> orderNos;  // 주문번호 목록
}