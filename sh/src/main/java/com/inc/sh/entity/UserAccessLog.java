package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_access_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;
    
    @Column(name = "user_type", length = 10, nullable = false)
    private String userType;            // ERP / CUSTOMER
    
    @Column(name = "user_code", length = 50, nullable = false)
    private String userCode;            // 사용자코드
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;             // 본사코드
    
    @Column(name = "login_status", length = 10, nullable = false)
    private String loginStatus;         // SUCCESS / FAILURE
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;           // IP주소
    
    @Column(name = "access_time", nullable = false)
    @CreationTimestamp
    private LocalDateTime accessTime;   // 접속시간
    
    @Column(name = "failure_reason", length = 100)
    private String failureReason;       // 실패사유
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 생성일시
}