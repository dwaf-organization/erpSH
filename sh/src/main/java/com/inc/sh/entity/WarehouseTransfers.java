package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseTransfers {
    
    @Id
    @Column(name = "transfer_code", length = 250, nullable = false)
    private String transferCode;
    
    @Column(name = "transfer_date", length = 250, nullable = false)
    private String transferDate;
    
    @Column(name = "from_warehouse_code", nullable = false)
    private Integer fromWarehouseCode;
    
    @Column(name = "to_warehouse_code", nullable = false)
    private Integer toWarehouseCode;
    
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