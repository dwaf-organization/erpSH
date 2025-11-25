package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "virtual_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "virtual_account_code")
    private Integer virtualAccountCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "linked_customer_code")
    private Integer linkedCustomerCode;
    
    @Column(name = "virtual_account_num", length = 250, nullable = false)
    private String virtualAccountNum;
    
    @Column(name = "virtual_account_status", length = 250, nullable = false)
    private String virtualAccountStatus;
    
    @Column(name = "bank_name", length = 250, nullable = false)
    private String bankName;
    
    @Column(name = "open_dt", length = 250, nullable = false)
    private String openDt;
    
    @Column(name = "close_dt", length = 250)
    private String closeDt;
    
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
    
    // FK 관계 (필요시 활성화)
    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hq_code", insertable = false, updatable = false)
    private Headquarter headquarter;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_customer_code", insertable = false, updatable = false)
    private Customer customer;
    */
}