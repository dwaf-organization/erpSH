package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreReviewStatsRespDto {
    
    /**
     * 매장별 리뷰 통계 목록
     */
    private List<StoreReviewStat> storeReviewStats;
    
    /**
     * 매장별 리뷰 통계 내부 클래스
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreReviewStat {
        
        /**
         * 번호 (1, 2, 3... 합계는 0)
         */
        private Integer rowNumber;
        
        /**
         * 매장명 (합계 행은 "합계")
         */
        private String storeName;
        
        // ========== 기간별 합계 ==========
        /**
         * 전체 리뷰수 합계 (3개 앱)
         */
        private Integer totalReviewCount;
        
        /**
         * 전체 평점 평균 (3개 앱)
         */
        private Double totalAvgRating;
        
        // ========== 배민 ==========
        /**
         * 배민 리뷰수
         */
        private Integer baeminReviewCount;
        
        /**
         * 배민 평점 평균
         */
        private Double baeminAvgRating;
        
        // ========== 요기요 ==========
        /**
         * 요기요 리뷰수
         */
        private Integer yogiyoReviewCount;
        
        /**
         * 요기요 평점 평균
         */
        private Double yogiyoAvgRating;
        
        // ========== 쿠팡이츠 ==========
        /**
         * 쿠팡이츠 리뷰수
         */
        private Integer coupangReviewCount;
        
        /**
         * 쿠팡이츠 평점 평균
         */
        private Double coupangAvgRating;
        
        /**
         * 매장코드 (내부용, 프론트에서는 숨김)
         */
        private Integer storePlatformCode;
    }
}