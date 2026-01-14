package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppSaveReqDto {
    
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
    
    /**
     * 플랫폼 매장 ID
     */
    private String storeId;
    
    /**
     * 로그인 ID
     */
    private String loginId;
    
    /**
     * 로그인 패스워드
     */
    private String loginPassword;
}