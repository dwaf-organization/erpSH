package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "monthly_inventory_closing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyInventoryClosing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closing_code")
    private Integer closingCode;
    
    @Column(name = "warehouse_item_code", nullable = false)
    private Integer warehouseItemCode;
    
    @Column(name = "warehouse_code", nullable = false)
    private Integer warehouseCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "closing_ym", length = 250, nullable = false)
    private String closingYm;
    
    @Column(name = "opening_quantity")
    private Integer openingQuantity = 0;
    
    @Column(name = "opening_amount")
    private Integer openingAmount = 0;
    
    @Column(name = "in_quantity")
    private Integer inQuantity = 0;
    
    @Column(name = "in_amount")
    private Integer inAmount = 0;
    
    @Column(name = "out_quantity")
    private Integer outQuantity = 0;
    
    @Column(name = "out_amount")
    private Integer outAmount = 0;
    
    @Column(name = "cal_quantity")
    private Integer calQuantity = 0;
    
    @Column(name = "cal_amount")
    private Integer calAmount = 0;
    
    @Column(name = "actual_quantity")
    private Integer actualQuantity = 0;
    
    @Column(name = "actual_unit_price")
    private Integer actualUnitPrice = 0;
    
    @Column(name = "actual_amount")
    private Integer actualAmount = 0;
    
    @Column(name = "diff_quantity")
    private Integer diffQuantity = 0;
    
    @Column(name = "diff_amount")
    private Integer diffAmount = 0;
    
    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;
    
    @Column(name = "closed_at", length = 250)
    private String closedAt;
    
    @Column(name = "closed_user", length = 250)
    private String closedUser;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}