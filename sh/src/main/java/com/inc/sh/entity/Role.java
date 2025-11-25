package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "role")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_code")
    private Integer roleCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "role_name", length = 250, nullable = false)
    private String roleName;
    
    @Column(name = "note", length = 250)
    private String note;
    
    @Column(name = "description", length = 250)
    private String description; // 적요
    
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
    */
}