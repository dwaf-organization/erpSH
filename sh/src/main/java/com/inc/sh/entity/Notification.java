package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_code")
    private Integer notificationCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "customer_name", length = 250, nullable = false)
    private String customerName;
    
    @Column(name = "reference_name", length = 100, nullable = false)
    private String referenceName;
    
    @Column(name = "reference_code", length = 250, nullable = false)
    private String referenceCode;
    
    @Column(name = "read_yn", nullable = false)
    @Builder.Default
    private Integer readYn = 0; // 0=안읽음, 1=읽음
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}