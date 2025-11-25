package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deposits {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "deposit_id")
    private Integer depositId;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "virtual_account_code", nullable = false)
    private Integer virtualAccountCode;
    
    @Column(name = "deposit_date", length = 250, nullable = false)
    private String depositDate;
    
    @Column(name = "deposit_amount", nullable = false)
    private Integer depositAmount;
    
    @Column(name = "deposit_method", length = 250)
    private String depositMethod;
    
    @Column(name = "depositor_name", length = 250)
    private String depositorName;
    
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