package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_code")
    private Integer customerCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "brand_code", nullable = false)
    private Integer brandCode;
    
    @Column(name = "virtual_account_code")
    private Integer virtualAccountCode;
    
    @Column(name = "customer_name", length = 250, nullable = false)
    private String customerName;
    
    @Column(name = "owner_name", length = 250, nullable = false)
    private String ownerName;
    
    @Column(name = "biz_num", length = 250, nullable = false)
    private String bizNum;
    
    @Column(name = "zip_code", length = 250)
    private String zipCode;
    
    @Column(name = "addr", length = 250)
    private String addr;
    
    @Column(name = "biz_type", length = 250)
    private String bizType;
    
    @Column(name = "biz_sector", length = 250)
    private String bizSector;
    
    @Column(name = "email", length = 250)
    private String email;
    
    @Column(name = "tel_num", length = 250)
    private String telNum;
    
    @Column(name = "mobile_num", length = 250)
    private String mobileNum;
    
    @Column(name = "fax_num", length = 250)
    private String faxNum; // 부가세여부 (스키마상 fax_num이지만 실제 의미는 부가세여부)
    
    @Column(name = "tax_invoice_yn", nullable = false)
    private String taxInvoiceYn; // Y=발행, N=발행
    
    @Column(name = "tax_invoice_name", length = 250)
    private String taxInvoiceName;
    
    @Column(name = "reg_dt", length = 250, nullable = false)
    private String regDt;
    
    @Column(name = "close_dt", length = 250)
    private String closeDt; // 종료일자 (삭제시 현재일 입력)
    
    @Column(name = "print_note", length = 250)
    private String printNote;
    
    @Column(name = "bank_name", length = 250)
    private String bankName;
    
    @Column(name = "account_holder", length = 250)
    private String accountHolder;
    
    @Column(name = "account_num", length = 250)
    private String accountNum;
    
    @Column(name = "dist_center_code", nullable = false)
    private Integer distCenterCode;
    
    @Column(name = "delivery_weekday", length = 7, nullable = false)
    @Builder.Default
    private String deliveryWeekday = "1111111"; // 월화수목금토일 (1=배송가능, 0=배송불가)
    
    @Column(name = "deposit_type_code", nullable = false)
    @Builder.Default
    private Integer depositTypeCode = 0; // 0=후입금, 1=충전형
    
    @Column(name = "virtual_account", length = 250)
    private String virtualAccount;
    
    @Column(name = "virtual_bank_name", length = 250)
    private String virtualBankName;
    
    @Column(name = "balance_amt", nullable = false)
    @Builder.Default
    private Integer balanceAmt = 0;
    
    @Column(name = "hq_memo", length = 250)
    private String hqMemo;
    
    @Column(name = "credit_limit", nullable = false)
    @Builder.Default
    private Integer creditLimit = 0;
    
    @Column(name = "collection_day", nullable = false)
    @Builder.Default
    private Integer collectionDay = 0;
    
    @Column(name = "order_block_yn", nullable = false)
    @Builder.Default
    private Integer orderBlockYn = 0; // 0=정상, 1=차단
    
    @Column(name = "order_block_reason", length = 250)
    private String orderBlockReason;
    
    @Column(name = "order_block_dt", length = 250)
    private String orderBlockDt;
    
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
    @JoinColumn(name = "brand_code", insertable = false, updatable = false)
    private BrandInfo brandInfo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dist_center_code", insertable = false, updatable = false)
    private DistCenter distCenter;
    */
}