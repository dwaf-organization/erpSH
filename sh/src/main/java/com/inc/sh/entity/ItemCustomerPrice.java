package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "item_customer_price")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCustomerPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_customer_price_code")
    private Integer itemCustomerPriceCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "customer_supply_price", nullable = false)
    private Integer customerSupplyPrice;
    
    @Column(name = "supply_price")
    @Builder.Default
    private Integer supplyPrice = 0; // 공급가액
    
    @Column(name = "tax_amount") 
    @Builder.Default
    private Integer taxAmount = 0; // 부가세액
    
    @Column(name = "taxable_amount")
    @Builder.Default
    private Integer taxableAmount = 0; // 과세액(과세 공급가액)
    
    @Column(name = "duty_free_amount")
    @Builder.Default
    private Integer dutyFreeAmount = 0; // 면세액
    
    @Column(name = "total_amount")
    @Builder.Default
    private Integer totalAmount = 0; // 총액
    
    @Column(name = "start_dt", length = 250, nullable = false)
    private String startDt;
    
    @Column(name = "end_dt", length = 250)
    private String endDt;
    
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
    @JoinColumn(name = "item_code", insertable = false, updatable = false)
    private Item item;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_code", insertable = false, updatable = false)
    private Customer customer;
    */
}