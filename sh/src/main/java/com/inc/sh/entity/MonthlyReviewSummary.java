package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_review_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReviewSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "monthly_review_summary_code")
    private Integer monthlyReviewSummaryCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "platform", nullable = false, length = 250)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "summary_year_month", nullable = false, length = 7)
    private String summaryYearMonth; // 집계년월(YYYY-MM)

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0; // 리뷰수

    @Column(name = "average_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageRating = BigDecimal.ZERO; // 평균별점

    @Column(name = "total_rating")
    @Builder.Default
    private Integer totalRating = 0; // 총별점합계

    @Column(name = "reply_count")
    @Builder.Default
    private Integer replyCount = 0; // 댓글수(사장님댓글수)

    @Column(name = "image_review_count")
    @Builder.Default
    private Integer imageReviewCount = 0; // 이미지리뷰수

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