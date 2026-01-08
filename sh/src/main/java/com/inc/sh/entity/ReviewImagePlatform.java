package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_image_platform")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImagePlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_image_platform_code")
    private Integer reviewImagePlatformCode;

    @Column(name = "review_platform_code", nullable = false)
    private Integer reviewPlatformCode;

    @Column(name = "store_platform_code", nullable = false)
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "seq", nullable = false)
    @Builder.Default
    private Integer seq = 1; // 이미지순서

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl; // 이미지URL

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