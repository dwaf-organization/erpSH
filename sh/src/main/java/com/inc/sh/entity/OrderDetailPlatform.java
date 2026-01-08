package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_detail_platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetailPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_platform_code")
    private Integer orderDetailPlatformCode;

    @Column(name = "order_platform_code", nullable = false)
    private Integer orderPlatformCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "seq", nullable = false)
    @Builder.Default
    private Integer seq = 1; // 메뉴순서

    @Column(name = "menu_name", nullable = false, length = 250)
    private String menuName; // 메뉴명

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1; // 수량

    @Column(name = "unit_price")
    @Builder.Default
    private Integer unitPrice = 0; // 단가

    @Column(name = "sale_price")
    @Builder.Default
    private Integer salePrice = 0; // 판매가

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