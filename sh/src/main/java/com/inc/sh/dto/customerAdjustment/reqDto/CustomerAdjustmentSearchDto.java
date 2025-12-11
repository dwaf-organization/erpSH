package com.inc.sh.dto.customerAdjustment.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentSearchDto {
    
    private Integer hqCode;                 // 본사코드 (완전일치, 필수)
    private Integer customerCode;           // 거래처코드 (완전일치)
    private String adjustmentDateStart;     // 조정일자 시작 (YYYYMMDD)
    private String adjustmentDateEnd;       // 조정일자 종료 (YYYYMMDD)
}