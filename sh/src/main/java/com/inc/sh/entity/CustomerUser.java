package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUser {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_user_code")
    private Integer customerUserCode;
    
    @Column(name = "customer_code", nullable = false)
    private Integer customerCode;
    
    @Column(name = "virtual_account_code")
    private Integer virtualAccountCode;
    
    @Column(name = "customer_user_id", length = 250, nullable = false)
    private String customerUserId;
    
    @Column(name = "customer_user_pw", length = 250, nullable = false)
    private String customerUserPw;
    
    @Column(name = "customer_user_name", length = 250, nullable = false)
    private String customerUserName;
    
    @Column(name = "contact_num", length = 250, nullable = false)
    private String contactNum;
    
    @Column(name = "email", length = 250)
    private String email;
    
    @Column(name = "end_yn", nullable = false)
    @Builder.Default
    private Integer endYn = 0; // 0=사용중, 1=종료
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}