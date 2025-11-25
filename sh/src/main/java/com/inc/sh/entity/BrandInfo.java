package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "brand_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_code")
    private Integer brandCode;

    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;

    @Column(name = "brand_name", nullable = false, length = 250)
    private String brandName;

    @Column(name = "note", length = 250)
    private String note;

    @Column(name = "description", length = 250)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}