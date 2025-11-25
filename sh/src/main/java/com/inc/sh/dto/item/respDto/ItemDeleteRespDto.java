package com.inc.sh.dto.item.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDeleteRespDto {
    
    private Integer itemCode;
    private String endDt; // 종료일자
}