package com.inc.sh.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_code")
    private Integer orderItemCode;
    
    @Column(name = "order_no", length = 250, nullable = false)
    private String orderNo;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "release_warehouse_code", nullable = false)
    private Integer releaseWarehouseCode;
    
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
    
    @Column(name = "current_stock_qty")
    private Integer currentStockQty;
    
    @Column(name = "order_qty", nullable = false)
    private Integer orderQty;
    
    @Column(name = "tax_target", length = 250)
    private String taxTarget;
    
    @Column(name = "warehouse_name", length = 250)
    private String warehouseName;
    
    @Column(name = "taxable_amt", nullable = false)
    private Integer taxableAmt;
    
    @Column(name = "tax_free_amt", nullable = false)
    private Integer taxFreeAmt;
    
    @Column(name = "supply_amt", nullable = false)
    private Integer supplyAmt;
    
    @Column(name = "vat_amt", nullable = false)
    private Integer vatAmt;
    
    @Column(name = "total_amt", nullable = false)
    private Integer totalAmt;
    
    @Column(name = "total_qty", nullable = false)
    private Integer totalQty;
    
    @Column(name = "returned_qty", nullable = false)
    @Builder.Default
    private Integer returnedQty = 0;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 반품 가능 수량 계산
     */
    public Integer getAvailableReturnQty() {
        return orderQty - returnedQty;
    }
}