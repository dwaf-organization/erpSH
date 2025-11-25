package com.inc.sh.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_code")
    private Integer menuCode;
    
    @Column(name = "menu_name", length = 250, nullable = false)
    private String menuName;
    
    @Column(name = "parent_id")
    private Integer parentId;
    
    @Column(name = "menu_level", nullable = false)
    private Integer menuLevel;
    
    @Column(name = "menu_order", nullable = false)
    @Builder.Default
    private Integer menuOrder = 0;
    
    @Column(name = "menu_path", length = 250)
    private String menuPath;
    
    @Column(name = "description", length = 250)
    private String description;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}