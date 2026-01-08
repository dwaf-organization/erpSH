package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "daily_sales_summary")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_sales_summary_code")
    private Integer dailySalesSummaryCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "platform", nullable = false, length = 250)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "summary_date", nullable = false, length = 10)
    private String summaryDate; // 집계일자(YYYY-MM-DD)

    @Column(name = "order_count")
    @Builder.Default
    private Integer orderCount = 0; // 주문건수

    @Column(name = "total_order_amount")
    @Builder.Default
    private Integer totalOrderAmount = 0; // 총주문금액

    @Column(name = "total_settle_amount")
    @Builder.Default
    private Integer totalSettleAmount = 0; // 총정산금액

    @Column(name = "total_delivery_amount")
    @Builder.Default
    private Integer totalDeliveryAmount = 0; // 총배달료

    @Column(name = "total_discount_amount")
    @Builder.Default
    private Integer totalDiscountAmount = 0; // 총할인금액

    @Column(name = "total_fee")
    @Builder.Default
    private Integer totalFee = 0; // 총수수료(주문중개수수료+카드수수료)

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