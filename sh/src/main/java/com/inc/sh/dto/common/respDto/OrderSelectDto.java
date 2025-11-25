package com.inc.sh.dto.common.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSelectDto {
    
    private String value;               // 주문번호
    private String label;               // 주문번호 (동일)
}