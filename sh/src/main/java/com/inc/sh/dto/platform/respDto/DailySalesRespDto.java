package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesRespDto {
    
    /**
     * 거래처별 일별매출 정보
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreDailySales {
        private Integer customerCode;           // 거래처코드
        private String customerName;            // 거래처명
        private Integer monthlyTotal;           // 월 합계매출 (전체 플랫폼 합계)
        private List<Integer> dailySales;       // 일별매출 배열 (1일~31일, 없으면 0, 전체 플랫폼 합계)
    }
    
    private String searchMonth;                 // 조회월 (YYYYMM)
    private Integer totalDays;                  // 해당월 총 일수 (28~31일)
    private List<StoreDailySales> stores;       // 거래처별 일별매출 목록
    
}