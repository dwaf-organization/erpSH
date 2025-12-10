package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @Column(name = "order_no", length = 250)
    private String orderNo;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "vehicle_code", nullable = false)
    private Integer vehicleCode;
    
    @Column(name = "dist_center_code", nullable = false)
    private Integer distCenterCode;
    
    @Column(name = "customer_name", length = 250, nullable = false)
    private String customerName;
    
    @Column(name = "biz_num", length = 250)
    private String bizNum;
    
    @Column(name = "zip_code", length = 250)
    private String zipCode;
    
    @Column(name = "addr", length = 250)
    private String addr;
    
    @Column(name = "owner_name", length = 250)
    private String ownerName;
    
    @Column(name = "tel_num", length = 250)
    private String telNum;
    
    @Column(name = "order_dt", length = 250, nullable = false)
    private String orderDt;
    
    @Column(name = "delivery_request_dt", length = 250, nullable = false)
    private String deliveryRequestDt;
    
    @Column(name = "delivery_amt")
    private Integer deliveryAmt;
    
    @Column(name = "dist_center_name", length = 250)
    private String distCenterName;
    
    @Column(name = "delivery_dt", length = 250)
    private String deliveryDt;
    
    @Column(name = "vehicle_name", length = 250)
    private String vehicleName;
    
    @Column(name = "delivery_status", length = 250, nullable = false)
    @Builder.Default
    private String deliveryStatus = "배송요청";
    
    @Column(name = "payment_status", length = 250, nullable = false)
    private String paymentStatus;
    
    @Column(name = "deposit_type_code", nullable = false)
    private Integer depositTypeCode;
    
    @Column(name = "payment_at", length = 250)
    private String paymentAt;
    
    @Column(name = "end_at", length = 250)
    private String endAt;
    
    @Column(name = "order_message", length = 250)
    private String orderMessage;
    
    @Column(name = "taxable_amt", nullable = false)
    @Builder.Default
    private Integer taxableAmt = 0;
    
    @Column(name = "tax_free_amt", nullable = false)
    @Builder.Default
    private Integer taxFreeAmt = 0;
    
    @Column(name = "supply_amt", nullable = false)
    @Builder.Default
    private Integer supplyAmt = 0;
    
    @Column(name = "vat_amt", nullable = false)
    @Builder.Default
    private Integer vatAmt = 0;
    
    @Column(name = "total_amt", nullable = false)
    private Integer totalAmt;
    
    @Column(name = "total_qty", nullable = false)
    private Integer totalQty;
    
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
    @JoinColumn(name = "customer_code", insertable = false, updatable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_code", insertable = false, updatable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dist_center_code", insertable = false, updatable = false)
    private DistCenter distCenter;
    */
}