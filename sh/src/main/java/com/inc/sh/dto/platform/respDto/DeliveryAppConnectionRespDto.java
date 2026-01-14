package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryAppConnectionRespDto {
    
    /**
     * 배달앱 연결 정보 리스트
     */
    private List<DeliveryAppConnection> deliveryAppConnections;
    
    /**
     * 배달앱 연결 정보 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryAppConnection {
        
        /**
         * 거래처코드
         */
        private Integer customerCode;
        
        /**
         * 거래처명
         */
        private String customerName;
        
        /**
         * 사업자번호
         */
        private String businessNumber;
        
        /**
         * 배민 연결 상태 (등록완료/미등록)
         */
        private String baeminStatus;
        
        /**
         * 요기요 연결 상태 (등록완료/미등록)
         */
        private String yogiyoStatus;
        
        /**
         * 쿠팡이츠 연결 상태 (등록완료/미등록)
         */
        private String coupangStatus;
    }
}