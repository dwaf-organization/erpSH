package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_holiday")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHoliday {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_holiday_code")
    private Integer deliveryHolidayCode;
    
    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "holiday_dt", length = 250)
    private String holidayDt; // 기본휴일: YYYY-MM-DD, 정기휴일: 00.00.00~00.00.00
    
    @Column(name = "holiday_name", length = 250)
    private String holidayName;
    
    @Column(name = "weekday", length = 250)
    private String weekday;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // FK 관계 (필요시 활성화)
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_code", insertable = false, updatable = false)
    private BrandInfo brandInfo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_code", insertable = false, updatable = false)
    private Headquarter headquarter;
    */
}