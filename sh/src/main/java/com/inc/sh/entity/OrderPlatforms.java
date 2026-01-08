package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_platforms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlatforms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_platform_code")
    private Integer orderPlatformCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "user_tr_no", length = 250)
    private String userTrNo; // 사용자거래고유번호

    @Column(name = "hyphen_tr_no", length = 250)
    private String hyphenTrNo; // 하이픈고유거래번호

    @Column(name = "platform", nullable = false, length = 250)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "order_no", nullable = false, length = 250)
    private String orderNo; // 주문번호

    @Column(name = "order_date", nullable = false, length = 250)
    private String orderDate; // 주문날짜

    @Column(name = "order_time", length = 250)
    private String orderTime; // 주문시간

    @Column(name = "order_division", length = 250)
    private String orderDivision; // 주문구분(성공, 실패)

    @Column(name = "order_name", length = 250)
    private String orderName; // 주문내역

    @Column(name = "delivery_type", length = 250)
    private String deliveryType; // 수령방법

    @Column(name = "payment_method", length = 250)
    private String paymentMethod; // 결제형태

    @Column(name = "order_amount")
    @Builder.Default
    private Integer orderAmount = 0; // 주문금액

    @Column(name = "delivery_amount")
    @Builder.Default
    private Integer deliveryAmount = 0; // 배달료

    @Column(name = "discount_amount")
    @Builder.Default
    private Integer discountAmount = 0; // 할인금액

    @Column(name = "coupon_amount")
    @Builder.Default
    private Integer couponAmount = 0; // 쿠폰금액

    @Column(name = "order_fee")
    @Builder.Default
    private Integer orderFee = 0; // 주문중개수수료

    @Column(name = "card_fee")
    @Builder.Default
    private Integer cardFee = 0; // 카드수수료

    @Column(name = "tax")
    @Builder.Default
    private Integer tax = 0; // 부가세

    @Column(name = "settle_date", length = 250)
    private String settleDate; // 정산일

    @Column(name = "settle_amount")
    @Builder.Default
    private Integer settleAmount = 0; // 정산금액

    @Column(name = "offline_order_amount")
    @Builder.Default
    private Integer offlineOrderAmount = 0; // 현장결제금액

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