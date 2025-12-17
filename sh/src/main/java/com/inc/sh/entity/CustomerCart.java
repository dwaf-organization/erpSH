package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_cart_code")
    private Integer customerCartCode;
    
    @Column(name = "customer_user_code", nullable = false)
    private Integer customerUserCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "virtual_account_code")
    private Integer virtualAccountCode;
    
    @Column(name = "warehouse_code", nullable = false)
    private Integer warehouseCode;
    
    @Column(name = "item_name", length = 250, nullable = false)
    private String itemName;
    
    @Column(name = "specification", length = 250)
    private String specification;
    
    @Column(name = "unit", length = 250, nullable = false)
    private String unit;
    
    @Column(name = "price_type", nullable = false)
    private Integer priceType;
    
    @Column(name = "order_unit_price", nullable = false)
    private Integer orderUnitPrice;
    
    @Column(name = "supply_price")
    @Builder.Default
    private Integer supplyPrice = 0;        // 공급가액
    
    @Column(name = "tax_amount")
    @Builder.Default
    private Integer taxAmount = 0;          // 부가세액
    
    @Column(name = "taxable_amount")
    @Builder.Default
    private Integer taxableAmount = 0;      // 과세액(과세 공급가액)
    
    @Column(name = "duty_free_amount")
    @Builder.Default
    private Integer dutyFreeAmount = 0;     // 면세액
    
    @Column(name = "total_amount")
    @Builder.Default
    private Integer totalAmount = 0;        // 총액
    
    @Column(name = "current_stock_qty")
    private Integer currentStockQty;
    
    @Column(name = "order_qty", nullable = false)
    private Integer orderQty;
    
    @Column(name = "tax_target", length = 250)
    private String taxTarget;
    
    @Column(name = "warehouse_name", length = 250)
    private String warehouseName;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}