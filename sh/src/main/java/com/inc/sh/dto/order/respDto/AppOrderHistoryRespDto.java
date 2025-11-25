package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppOrderHistoryRespDto {
    
    private OrderSummary summary;
    private List<OrderDto> orders;
    private PaginationInfo pagination;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private Integer totalCount;    // 총 주문건수
        private Integer totalAmount;   // 총 주문금액
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDto {
        private String orderNo;           // 주문번호
        private String orderDt;           // 주문일자 (yyyy-MM-dd)
        private String deliveryRequestDt; // 납기요청일 (yyyy-MM-dd)
        private String deliveryStatus;    // 배송상태
        private Integer totalAmt;         // 총금액
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationInfo {
        private Integer currentPage;    // 현재 페이지
        private Integer size;          // 페이지 크기
        private Long totalElements;    // 전체 데이터 수
        private Integer totalPages;    // 전체 페이지 수
        private Boolean hasNext;       // 다음 페이지 존재 여부
    }
}