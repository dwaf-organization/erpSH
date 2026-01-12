package com.inc.sh.dto.platform.respDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardReviewRespDto {
    
    /**
     * 채널별 리뷰 비율 (3개월)
     */
    private ChannelRatio channelRatio;
    
    /**
     * 채널별 별점평균 (3개월)
     */
    private ChannelRatings channelRatings;
    
    /**
     * 채널별 리뷰추이 (12개월)
     */
    private List<MonthlyReviewTrend> channelTrend;
    
    /**
     * 평점 높은 매장 순위 (현재월, Top 10)
     */
    private List<StoreRanking> topRatedStores;
    
    /**
     * 리뷰 많은 매장 순위 (현재월, Top 10)
     */
    private List<StoreRanking> topReviewedStores;
    
    /**
     * 채널별 리뷰 비율
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelRatio {
        private Integer baeminCount;     // 배민 리뷰수
        private String baeminRate;       // 배민 비율 (예: "50.0%")
        private Integer yogiyoCount;     // 요기요 리뷰수
        private String yogiyoRate;       // 요기요 비율
        private Integer coupangCount;    // 쿠팡이츠 리뷰수
        private String coupangRate;      // 쿠팡이츠 비율
        private Integer totalCount;      // 전체 리뷰수
    }
    
    /**
     * 채널별 별점평균
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChannelRatings {
        private Double baeminRating;     // 배민 평점 (예: 4.9)
        private Double yogiyoRating;     // 요기요 평점
        private Double coupangRating;    // 쿠팡이츠 평점
    }
    
    /**
     * 월별 리뷰추이
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyReviewTrend {
        private String month;            // 월 (예: "2026년 1월")
        private Integer baeminCount;     // 배민 리뷰수
        private Integer yogiyoCount;     // 요기요 리뷰수
        private Integer coupangCount;    // 쿠팡이츠 리뷰수
        private Integer totalCount;      // 전체 리뷰수
    }
    
    /**
     * 매장 순위 (평점 또는 리뷰수)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StoreRanking {
        private Integer rank;            // 순위
        private String storeName;        // 매장명
        private Double rating;           // 평점 (평점순위용)
        private Integer reviewCount;     // 리뷰수 (리뷰수순위용)
    }
}