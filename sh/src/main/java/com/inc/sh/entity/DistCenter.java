package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dist_center")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenter {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dist_center_code")
    private Integer distCenterCode;
    
    @Column(name = "hq_code", nullable = false)
    private Integer hqCode;
    
    @Column(name = "dist_center_name", length = 250, nullable = false)
    private String distCenterName;
    
    @Column(name = "zip_code", length = 250)
    private String zipCode;
    
    @Column(name = "addr", length = 250)
    private String addr;
    
    @Column(name = "tel_num", length = 250)
    private String telNum;
    
    @Column(name = "manager_name", length = 250)
    private String managerName;
    
    @Column(name = "manager_contact", length = 250)
    private String managerContact;
    
    @Column(name = "use_yn", nullable = false)
    @Builder.Default
    private Integer useYn = 1; // 0=미사용, 1=사용
    
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
    */
}