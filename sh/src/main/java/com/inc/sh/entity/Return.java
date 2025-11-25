package com.inc.sh.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "`return`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {
    
    @Id
    @Column(name = "return_no", length = 250, nullable = false)
    private String returnNo;
    
    @Column(name = "return_customer_code", nullable = false)
    private Integer returnCustomerCode;
    
    @Column(name = "item_code", nullable = false)
    private Integer itemCode;
    
    @Column(name = "receive_warehouse_code")
    private Integer receiveWarehouseCode;
    
    @Column(name = "return_customer_name", length = 250)
    private String returnCustomerName;
    
    @Column(name = "return_request_dt", length = 250, nullable = false)
    private String returnRequestDt;
    
    @Column(name = "item_name", length = 250)
    private String itemName;
    
    @Column(name = "specification", length = 250)
    private String specification;
    
    @Column(name = "unit", length = 250)
    private String unit;
    
    @Column(name = "qty", nullable = false)
    @Builder.Default
    private Integer qty = 1;
    
    @Column(name = "price_type")
    private Integer priceType;
    
    @Column(name = "unit_price")
    private Integer unitPrice;
    
    @Column(name = "supply_price")
    private Integer supplyPrice;
    
    @Column(name = "vat_amt")
    private Integer vatAmt;
    
    @Column(name = "total_amt")
    private Integer totalAmt;
    
    @Column(name = "return_message", length = 250)
    private String returnMessage;
    
    @Column(name = "reply_message", length = 250)
    private String replyMessage;
    
    @Column(name = "note", length = 250)
    private String note;
    
    @Column(name = "progress_status", length = 250, nullable = false)
    @Builder.Default
    private String progressStatus = "미승인";
    
    @Column(name = "warehouse_name", length = 250)
    private String warehouseName;
    
    @Column(name = "return_approve_dt", length = 250)
    private String returnApproveDt;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @Column(name = "order_item_code", nullable = false)
    private Integer orderItemCode;
    
    @Column(name = "order_no", length = 250, nullable = false)
    private String orderNo;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}