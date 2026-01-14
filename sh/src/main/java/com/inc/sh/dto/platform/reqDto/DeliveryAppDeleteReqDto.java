package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppDeleteReqDto {
    
    /**
     * 거래처코드 (본사코드)
     */
    private Integer hqCode;
    
    /**
     * 거래처코드
     */
    private Integer customerCode;
    
    /**
     * 플랫폼 (배민/요기요/쿠팡이츠)
     */
    private String platform;
}