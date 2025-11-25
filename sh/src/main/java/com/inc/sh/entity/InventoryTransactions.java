package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_code")
    private Integer transactionCode;
    
    @Column(name = "warehouse_item_code", nullable = false)
    private Integer warehouseItemCode;
    
    @Column(name = "warehouse_code", nullable = false)
    private Integer warehouseCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "transaction_date", length = 250, nullable = false)
    private String transactionDate;
    
    @Column(name = "transaction_type", length = 250, nullable = false)
    private String transactionType;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price")
    private Integer unitPrice = 0;
    
    @Column(name = "amount")
    private Integer amount = 0;
    
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