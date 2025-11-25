package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @Column(name = "user_code", length = 20, nullable = false)
    private String userCode; // 사번 (2511001 형태)
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "role_code", nullable = false)
    private Integer roleCode;
    
    @Column(name = "user_name", length = 250, nullable = false)
    private String userName;
    
    @Column(name = "user_pw", length = 250, nullable = false)
    private String userPw;
    
    @Column(name = "phone1", length = 250, nullable = false)
    private String phone1;
    
    @Column(name = "phone2", length = 250)
    private String phone2;
    
    @Column(name = "email", length = 250, nullable = false)
    private String email;
    
    @Column(name = "resignation_dt", length = 250)
    private String resignationDt;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}