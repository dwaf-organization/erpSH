package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppInfoReqDto {
    
    /**
     * 거래처코드 (본사코드)
     */
    private Integer hqCode;
    
    /**
     * 거래처코드
     */
    private Integer customerCode;
}