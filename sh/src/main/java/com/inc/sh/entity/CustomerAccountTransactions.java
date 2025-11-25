package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_account_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountTransactions {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_code")
    private Integer transactionCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "virtual_account_code", nullable = false)
    private Integer virtualAccountCode;
    
    @Column(name = "transaction_date", length = 250, nullable = false)
    private String transactionDate;
    
    @Column(name = "transaction_type", length = 250, nullable = false)
    private String transactionType;
    
    @Column(name = "amount", nullable = false)
    private Integer amount;
    
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;
    
    @Column(name = "reference_type", length = 250)
    private String referenceType;
    
    @Column(name = "reference_id", length = 250)
    private String referenceId;
    
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