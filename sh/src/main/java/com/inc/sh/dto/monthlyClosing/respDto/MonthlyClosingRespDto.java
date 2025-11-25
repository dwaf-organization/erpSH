package com.inc.sh.dto.monthlyClosing.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyClosingRespDto {
    
    private Integer warehouseCode;      // 창고코드
    private String warehouseName;       // 창고명
    private String closingYm;           // 마감년월
    private String closedAt;            // 확정일자 (날짜만: 2025-11-14)
    private Integer isClosed;           // 마감여부 (1=마감, 0=미마감)
    private String closedUser;          // 마감처리자
}