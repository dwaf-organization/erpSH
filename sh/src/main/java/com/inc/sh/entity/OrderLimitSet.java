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
@Table(name = "order_limit_set")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitSet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "limit_code")
    private Integer limitCode;
    
    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "day_name", nullable = false, length = 5)
    private String dayName;
    
    @Column(name = "limit_start_time", nullable = false, length = 10)
    private String limitStartTime;
    
    @Column(name = "limit_end_time", nullable = false, length = 10)
    private String limitEndTime;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 다중 DTO의 개별 아이템으로 업데이트
     */
    public void update(OrderLimitSaveReqDto.OrderLimitItemDto itemDto, Integer brandCode, Integer hqCode) {
        this.brandCode = brandCode;
        this.hqCode = hqCode;
        this.dayName = itemDto.getDayName();
        this.limitStartTime = itemDto.getLimitStartTime();
        this.limitEndTime = itemDto.getLimitEndTime();
    }
    
    /**
     * 기존 단일 DTO와 호환성 유지 (필요시 사용)
     */
    public void updateFromSingleDto(OrderLimitSaveReqDto dto) {
        this.brandCode = dto.getBrandCode();
        this.hqCode = dto.getHqCode();
        // limits 배열이 있는 경우 첫 번째 아이템 사용
        if (dto.getLimits() != null && !dto.getLimits().isEmpty()) {
            OrderLimitSaveReqDto.OrderLimitItemDto firstItem = dto.getLimits().get(0);
            this.dayName = firstItem.getDayName();
            this.limitStartTime = firstItem.getLimitStartTime();
            this.limitEndTime = firstItem.getLimitEndTime();
        }
    }
}