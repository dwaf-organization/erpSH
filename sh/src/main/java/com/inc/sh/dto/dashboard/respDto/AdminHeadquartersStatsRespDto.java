package com.inc.sh.dto.dashboard.respDto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminHeadquartersStatsRespDto {
    
    private Integer hqCode;             // 본사코드
    private String hqName;              // 본사명 (companyName)
    private String businessNumber;      // 사업자번호 (bizNum)
    private Long customerCount;         // 거래처 수
    private Long itemCount;             // 품목 수
    private Long brandCount;            // 브랜드 수
    private Long userCount;             // 사용자 수
    private LocalDateTime createdAt;    // 생성일시
}