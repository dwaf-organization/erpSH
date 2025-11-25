package com.inc.sh.dto.customerAdjustment.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentSearchDto {
    
    private Integer customerCode;       // 거래처코드 (완전일치)
    private String adjustmentDate;      // 조정일자 (YYYYMMDD)
}