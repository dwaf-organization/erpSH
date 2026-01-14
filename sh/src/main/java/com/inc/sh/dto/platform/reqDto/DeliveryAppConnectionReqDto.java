package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppConnectionReqDto {
    
    /**
     * 거래처코드 (본사코드)
     */
    private Integer hqCode;
    
    /**
     * 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     */
    private Integer brandCode;
    
    /**
     * 페이지 번호 (0부터 시작)
     */
    private Integer page;
}