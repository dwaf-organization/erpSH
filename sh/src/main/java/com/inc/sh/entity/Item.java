package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_code")
    private Integer itemCode;
    
    @Column(name = "category_code", nullable = false)
    private Integer categoryCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "item_name", length = 250, nullable = false)
    private String itemName;
    
    @Column(name = "specification", length = 250)
    private String specification;
    
    @Column(name = "purchase_unit", length = 250, nullable = false)
    private String purchaseUnit;
    
    @Column(name = "vat_type", length = 250, nullable = false)
    private String vatType;
    
    @Column(name = "vat_detail", length = 250)
    private String vatDetail;
    
    @Column(name = "reg_dt", length = 250, nullable = false)
    private String regDt;
    
    @Column(name = "end_dt", length = 250)
    private String endDt;
    
    @Column(name = "origin", length = 250)
    private String origin;
    
    @Column(name = "price_type", nullable = false)
    private Integer priceType; // 1=납품싯가, 2=납품단가
    
    @Column(name = "base_price", nullable = false)
    private Integer basePrice;
    
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
    
    @Column(name = "previous_price")
    private Integer previousPrice;
    
    @Column(name = "hq_memo", length = 250)
    private String hqMemo;
    
    @Column(name = "order_available_yn", nullable = false)
    @Builder.Default
    private Integer orderAvailableYn = 1; // 0=전체불가, 1=전체가능, 2=선택불가
    
    @Column(name = "min_order_qty", nullable = false)
    @Builder.Default
    private Integer minOrderQty = 1;
    
    @Column(name = "max_order_qty", nullable = false)
    @Builder.Default
    private Integer maxOrderQty = 9999;
    
    @Column(name = "deadline_day", nullable = false)
    @Builder.Default
    private Integer deadlineDay = 1;
    
    @Column(name = "deadline_time", length = 10, nullable = false)
    @Builder.Default
    private String deadlineTime = "18:00";
    
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
    @JoinColumn(name = "category_code", insertable = false, updatable = false)
    private ItemCategory itemCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_code", insertable = false, updatable = false)
    private Headquarter headquarter;
    */
}