package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "order_item_return_status")
@Immutable  // 읽기 전용 뷰
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemReturnStatus {
    
    @Id
    @Column(name = "order_item_code")
    private Integer orderItemCode;
    
    @Column(name = "order_no")
    private String orderNo;
    
    @Column(name = "item_code")
    private Integer itemCode;
    
    @Column(name = "item_name")
    private String itemName;
    
    @Column(name = "order_qty")
    private Integer orderQty;
    
    @Column(name = "returned_qty")
    private Integer returnedQty;
    
    @Column(name = "available_return_qty")
    private Integer availableReturnQty;
    
    @Column(name = "delivery_status")
    private String deliveryStatus;
    
    @Column(name = "customer_code")
    private Integer customerCode;
    
    @Column(name = "customer_name")
    private String customerName;
}