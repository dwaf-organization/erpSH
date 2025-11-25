package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "headquarter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Headquarter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hq_code")
    private Integer hqCode;
    
    @Column(name = "hq_access_code", length = 12, unique = true)
    private String hqAccessCode; // SH_1111_1111 형식의 본사접속코드
    
    @Column(name = "company_name", length = 250, nullable = false)
    private String companyName;
    
    @Column(name = "corp_reg_num", length = 250)
    private String corpRegNum;
    
    @Column(name = "biz_num", length = 250, nullable = false)
    private String bizNum;
    
    @Column(name = "ceo_name", length = 250, nullable = false)
    private String ceoName;
    
    @Column(name = "zip_code", length = 250)
    private String zipCode;
    
    @Column(name = "addr", length = 250)
    private String addr;
    
    @Column(name = "biz_type", length = 250)
    private String bizType;
    
    @Column(name = "biz_item", length = 250)
    private String bizItem;
    
    @Column(name = "tel_num", length = 250)
    private String telNum;
    
    @Column(name = "inquiry_tel_num", length = 255, nullable = false)
    private String inquiryTelNum;
    
    @Column(name = "fax_num", length = 250)
    private String faxNum;
    
    @Column(name = "homepage", length = 250)
    private String homepage;
    
    @Column(name = "bank_name", length = 250)
    private String bankName;
    
    @Column(name = "account_num", length = 250)
    private String accountNum;
    
    @Column(name = "account_holder", length = 250)
    private String accountHolder;
    
    @Column(name = "logistics_type", nullable = false)
    @Builder.Default
    private Integer logisticsType = 0;
    
    @Column(name = "price_display_type", length = 50, nullable = false)
    @Builder.Default
    private String priceDisplayType = "납품단가";
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}