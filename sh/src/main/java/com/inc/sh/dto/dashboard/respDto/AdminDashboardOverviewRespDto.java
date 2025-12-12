package com.inc.sh.dto.dashboard.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardOverviewRespDto {
    
    private Long totalHeadquarters;     // 총 본사수
    private Long totalUsers;            // 전체 사용자수  
    private Long totalBrands;           // 총 브랜드수
    private Long totalCustomers;        // 총 거래처수
    private Long totalItems;            // 총 품목수
}