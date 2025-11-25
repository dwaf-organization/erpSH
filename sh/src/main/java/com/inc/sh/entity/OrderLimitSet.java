package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.inc.sh.dto.orderLimitSet.reqDto.OrderLimitSaveReqDto;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_limit_set") // 테이블명
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_code")
    private Integer limitCode; // PRIMARY KEY

    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;

    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;

    @Column(name = "day_name", nullable = false, length = 5)
    private String dayName; // 요일명(월,화,수,목,금,토,일)

    @Column(name = "limit_start_time", nullable = false, length = 10)
    private String limitStartTime; // 제한시작시간

    @Column(name = "limit_end_time", nullable = false, length = 10)
    private String limitEndTime; // 제한종료시간

    @Column(name = "description", length = 250)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public void update(OrderLimitSaveReqDto dto) {
        this.brandCode = dto.getBrandCode();
        this.hqCode = dto.getHqCode();
        this.dayName = dto.getDayName();
        this.limitStartTime = dto.getLimitStartTime();
        this.limitEndTime = dto.getLimitEndTime();
    }
}