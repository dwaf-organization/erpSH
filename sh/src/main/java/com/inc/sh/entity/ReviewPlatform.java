package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "review_platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewPlatform {

    @Id
    @Column(name = "review_platform_code", nullable = false)
    private Integer reviewPlatformCode; // AUTO_INCREMENT가 아닌 것 같음

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "platform", nullable = false, length = 50)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "review_date", nullable = false)
    private LocalDate reviewDate; // 리뷰날짜

    @Column(name = "review_time")
    private LocalTime reviewTime; // 리뷰시간

    @Column(name = "rating", nullable = false)
    private Integer rating; // 별점

    @Column(name = "order_menu", length = 250)
    private String orderMenu; // 주문내역

    @Column(name = "content", length = 500)
    private String content; // 리뷰내용

    @Column(name = "owner_reply_content", length = 500)
    private String ownerReplyContent; // 사장님댓글

    @Column(name = "owner_reply_date")
    private LocalDate ownerReplyDate; // 사장님댓글날짜

    @Column(name = "owner_reply_time")
    private LocalTime ownerReplyTime; // 사장님댓글시간

    @Column(name = "has_images", length = 1, nullable = false)
    private String hasImages; // 이미지포함여부(Y/N)

    @Column(name = "raw_data", columnDefinition = "LONGTEXT")
    private String rawData; // 원본응답 (JSON)

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