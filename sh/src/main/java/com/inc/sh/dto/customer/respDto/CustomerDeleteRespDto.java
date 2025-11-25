package com.inc.sh.dto.customer.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeleteRespDto {
    
    private Integer customerCode;
    private String closeDt;
}