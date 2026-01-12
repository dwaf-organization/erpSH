package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSalesRespDto {
    
    /**
     * 월별 매출/주문수 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyData {
        private String month;               // 월 (한글 형식: "2026년 1월")
        private Long value;                 // 값 (매출액 또는 주문수)
    }
    
    /**
     * 배달앱 비율 데이터
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryRateData {
        private String month;               // 월 (한글 형식: "2026년 1월")
        private String baeminRate;          // 배민 비율 (예: "83.5%")
        private String coupangRate;         // 쿠팡이츠 비율 (예: "12.2%")
        private String yogiyoRate;          // 요기요 비율 (예: "4.3%")
    }
    
    private List<MonthlyData> sales3Month;  // 브랜드별 3개월 매출액
    private List<MonthlyData> order3Month;  // 브랜드별 3개월 주문수
    private List<MonthlyData> sales12Month; // 매출액 추이 12개월
    private List<MonthlyData> order12Month; // 주문수 추이 12개월
    private List<DeliveryRateData> deliveryAnalysis; // 5개월 배달앱 매출비율
    
}