package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_limit_customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLimitCustomer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_limit_customer_code")
    private Integer orderLimitCustomerCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "limit_reason", length = 250)
    private String limitReason;
    
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