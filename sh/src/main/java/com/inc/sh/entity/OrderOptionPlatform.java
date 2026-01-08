package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_option_platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderOptionPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_option_platform_code")
    private Integer orderOptionPlatformCode;

    @Column(name = "order_detail_platform_code", nullable = false)
    private Integer orderDetailPlatformCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "seq", nullable = false)
    @Builder.Default
    private Integer seq = 1; // 옵션순서

    @Column(name = "option_name", nullable = false, length = 250)
    private String optionName; // 옵션명

    @Column(name = "option_price")
    @Builder.Default
    private Integer optionPrice = 0; // 옵션가격

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