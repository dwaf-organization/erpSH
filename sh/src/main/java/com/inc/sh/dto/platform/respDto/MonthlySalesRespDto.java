package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySalesRespDto {
    
    /**
     * 거래처별 월간매출 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreMonthlySales {
        private Integer customerCode;           // 거래처코드
        private String customerName;            // 거래처명
        private String month;                   // 월 (YYYYMM)
        private Integer totalSales;             // 총매출 (전체 플랫폼 합계)
        private Integer orderCount;             // 주문수 (전체 플랫폼 합계)
        private Integer avgSales;               // 평균매출 (총매출÷주문수)
    }
    
    /**
     * 전체 합계 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TotalSummary {
        private Integer totalSales;             // 전체 총매출
        private Integer totalOrderCount;        // 전체 주문수
        private Integer averageSalesPerStore;   // 거래처평균매출 (총매출÷거래처수)
        private Integer storeCount;             // 총 거래처수
    }
    
    private String startMonth;                  // 조회 시작월
    private String endMonth;                    // 조회 종료월
    private TotalSummary summary;               // 전체 합계 정보
    private List<StoreMonthlySales> stores;     // 거래처별 월간매출 목록
    
}