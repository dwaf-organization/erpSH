package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "store_platforms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorePlatforms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_platform_code")
    private Integer storePlatformCode;

    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode; // 본사코드

    @Column(name = "platform", nullable = false, length = 250)
    private String platform; // 쿠팡이츠, 배민, 요기요

    @Column(name = "platform_store_id", nullable = false, length = 250)
    private String platformStoreId;

    @Column(name = "platform_store_name", length = 250)
    private String platformStoreName;

    @Column(name = "login_id", length = 250)
    private String loginId; // 배달앱 로그인ID

    @Column(name = "login_password", length = 250)
    private String loginPassword; // 배달앱 로그인PW(암호화)

    @Column(name = "total_rating", length = 250)
    private String totalRating;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "comment_count")
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Integer isActive = 1; // 0=비활성화, 1=활성화

    @Column(name = "last_synced_at", length = 250)
    private String lastSyncedAt;

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