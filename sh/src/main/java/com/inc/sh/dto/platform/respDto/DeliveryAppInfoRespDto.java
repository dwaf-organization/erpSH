package com.inc.sh.dto.platform.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppInfoRespDto {
    
    /**
     * 거래처코드
     */
    private Integer customerCode;
    
    /**
     * 거래처명
     */
    private String customerName;
    
    /**
     * 배민 정보
     */
    private PlatformInfo baemin;
    
    /**
     * 요기요 정보
     */
    private PlatformInfo yogiyo;
    
    /**
     * 쿠팡이츠 정보
     */
    private PlatformInfo coupang;
    
    /**
     * 플랫폼 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlatformInfo {
        
        /**
         * 플랫폼 매장 ID
         */
        private String platformStoreId;
        
        /**
         * 로그인 ID
         */
        private String loginId;
        
        /**
         * 로그인 패스워드
         */
        private String loginPassword;
        
        /**
         * 등록 상태 (등록완료/미등록)
         */
        private String status;
    }
}