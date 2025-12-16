package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "district_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistrictAnalysis {
    
    @Id
    @Column(name = "district_code")
    private Integer districtCode;           // 구코드 (PK)
    
    @Column(name = "district_name", length = 50, nullable = false)
    private String districtName;            // 구명
    
    @Column(name = "max_sales", nullable = false)
    private Long maxSales;                  // 구최고매출
    
    @Column(name = "min_sales", nullable = false)
    private Long minSales;                  // 구최저매출
    
    @Column(name = "current_sales", nullable = false)
    private Long currentSales;              // 현재구의매출
    
    @Column(name = "growth_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal growthRate;          // 전월대비상승하락율(%)
    
    @Column(name = "growth_code", nullable = false)
    private Integer growthCode;             // 전월대비코드(1=상승, 0=하락)
    
    @Column(name = "peak_traffic_hour", length = 20, nullable = false)
    private String peakTrafficHour;         // 유동인구가장많은시간대
    
    @Column(name = "analysis_description", columnDefinition = "TEXT", nullable = false)
    private String analysisDescription;     // 분석해설
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;        // 생성일시
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;        // 수정일시
}