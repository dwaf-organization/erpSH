package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_wishlist")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerWishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_wishlist_code")
    private Integer customerWishlistCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "customer_user_code", nullable = false)
    private Integer customerUserCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "item_name", length = 250)
    private String itemName;
    
    @Column(name = "specification", length = 250)
    private String specification;
    
    @Column(name = "unit", length = 250)
    private String unit;
    
    @Column(name = "price_type")
    private Integer priceType;
    
    @Column(name = "order_unit_price")
    private Integer orderUnitPrice;
    
    @Column(name = "current_stock_qty")
    private Integer currentStockQty;
    
    @Column(name = "order_qty")
    private Integer orderQty;
    
    @Column(name = "tax_target", length = 250)
    private String taxTarget;
    
    @Column(name = "warehouse_code")
    private Integer warehouseCode;
    
    @Column(name = "warehouse_name", length = 250)
    private String warehouseName;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}