package com.inc.sh.dto.platform.respDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HyphenOrderRespDto {
    
    private CommonInfo common;      // 공통 응답 정보
    private DataInfo data;          // 데이터 정보
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommonInfo {
        private String userTrNo;        // 사용자거래번호
        private String hyphenTrNo;      // 하이픈거래번호
        private String errYn;           // 에러여부 (Y/N)
        private String errCd;           // 에러코드
        private String errMsg;          // 에러메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataInfo {
        private List<StoreInfo> storeList;      // 매장리스트
        private Integer totOrderCnt;            // 총주문건수
        private String startDate;               // 시작일자
        private String endDate;                 // 종료일자
        private Integer touchSucCnt;            // 터치성공건수
        private Integer touchCanCnt;            // 터치취소건수
        private Integer touchOrderAmt;          // 터치주문금액
        private Integer offlineOrderAmt;        // 오프라인주문금액
        private List<OrderData> touchOrderList; // 터치주문리스트
        private String errMsg;                  // 에러메시지
        private String errYn;                   // 에러여부
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreInfo {
        private String storeId;         // 매장ID
        private String storeName;       // 매장명
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderData {
        @JsonProperty("storeId")
        private String storeId;         // 매장ID (권한 검증용)
        
        @JsonProperty("orderNo")
        private String orderNo;         // 주문번호
        
        @JsonProperty("orderDt")
        private String orderDate;       // 주문날짜 (API: orderDt)
        
        @JsonProperty("orderTm")
        private String orderTime;       // 주문시간 (API: orderTm)
        
        @JsonProperty("orderDiv")
        private String orderDivision;   // 주문구분 (API: orderDiv)
        
        @JsonProperty("orderName")
        private String orderName;       // 주문내역
        
        @JsonProperty("deliveryType")
        private String deliveryType;    // 수령방법
        
        @JsonProperty("payMet")
        private String paymentMethod;   // 결제형태 (API: payMet)
        
        @JsonProperty("orderAmt")
        private String orderAmountStr;  // 주문금액 (API: orderAmt - String)
        
        @JsonProperty("deliveryAmt")
        private String deliveryAmountStr; // 배달료 (API: deliveryAmt - String)
        
        @JsonProperty("discntAmt")
        private String discountAmountStr; // 할인금액 (API: discntAmt - String)
        
        @JsonProperty("couponAmt")
        private String couponAmountStr;   // 쿠폰금액 (API: couponAmt - String)
        
        @JsonProperty("orderFee")
        private String orderFeeStr;      // 주문중개수수료 (API: orderFee - String)
        
        @JsonProperty("cardFee")
        private String cardFeeStr;       // 카드수수료 (API: cardFee - String)
        
        @JsonProperty("addTax")
        private String taxStr;           // 부가세 (API: addTax - String)
        
        @JsonProperty("settleDt")
        private String settleDate;       // 정산일 (API: settleDt)
        
        @JsonProperty("settleAmt")
        private String settleAmountStr;  // 정산금액 (API: settleAmt - String)
        
        @JsonProperty("payAmt")
        private String payAmountStr;     // 결제금액 (API: payAmt - String)
        
        // Integer 변환용 메서드들 (@JsonIgnore로 Jackson 충돌 방지)
        @JsonIgnore
        public Integer getOrderAmount() {
            return parseIntegerSafely(orderAmountStr);
        }
        
        @JsonIgnore
        public Integer getDeliveryAmount() {
            return parseIntegerSafely(deliveryAmountStr);
        }
        
        @JsonIgnore
        public Integer getDiscountAmount() {
            return parseIntegerSafely(discountAmountStr);
        }
        
        @JsonIgnore
        public Integer getCouponAmount() {
            return parseIntegerSafely(couponAmountStr);
        }
        
        @JsonIgnore
        public Integer getOrderFee() {
            return parseIntegerSafely(orderFeeStr);
        }
        
        @JsonIgnore
        public Integer getCardFee() {
            return parseIntegerSafely(cardFeeStr);
        }
        
        @JsonIgnore
        public Integer getTax() {
            return parseIntegerSafely(taxStr);
        }
        
        @JsonIgnore
        public Integer getSettleAmount() {
            return parseIntegerSafely(settleAmountStr);
        }
        
        @JsonIgnore
        public Integer getPayAmount() {
            return parseIntegerSafely(payAmountStr);
        }
        
        // 안전한 Integer 변환 (빈값/null 처리)
        private Integer parseIntegerSafely(String value) {
            if (value == null || value.trim().isEmpty() || "-".equals(value)) {
                return 0;
            }
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        // 주문상세 (메뉴정보) - 일단 주석처리
        // private List<OrderDetail> detailList;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderDetail {
        private Integer seq;            // 메뉴순서
        private String menuName;        // 메뉴명
        private Integer quantity;       // 수량
        private Integer unitPrice;      // 단가
        private Integer salePrice;      // 판매가
        
        // 옵션정보
        private List<OrderOption> options;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderOption {
        private Integer seq;            // 옵션순서
        private String optionName;      // 옵션명
        private Integer optionPrice;    // 옵션가격
    }
}