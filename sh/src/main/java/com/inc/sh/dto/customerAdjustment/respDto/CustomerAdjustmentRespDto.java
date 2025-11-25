package com.inc.sh.dto.customerAdjustment.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentRespDto {
    
    private Integer transactionCode;    // 거래내역코드
    private Integer customerCode;       // 거래처코드
    private String customerName;        // 거래처명
    private String adjustmentDate;      // 조정일자 (YYYY-MM-DD)
    private Integer adjustmentAmount;   // 조정금액 (+, - 가능)
    private String note;                // 비고
}