package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_ranking_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreRankingCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_ranking_cache_code")
    private Integer storeRankingCacheCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "ranking_type", nullable = false, length = 50)
    private String rankingType; // 별점순위, 리뷰수순위

    @Column(name = "ranking_period", nullable = false, length = 50)
    private String rankingPeriod; // 최근1개월, 최근3개월, 최근1년

    @Column(name = "platform", nullable = false, length = 250)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "store_name", length = 250)
    private String storeName;

    @Column(name = "metric_value", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal metricValue = BigDecimal.ZERO; // 지표값(별점 또는 리뷰수)

    @Column(name = "rank_number")
    @Builder.Default
    private Integer rankNumber = 0; // 순위

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt; // 마지막업데이트일시

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}