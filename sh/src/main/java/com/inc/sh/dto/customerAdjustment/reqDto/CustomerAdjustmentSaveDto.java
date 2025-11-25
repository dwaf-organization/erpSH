package com.inc.sh.dto.customerAdjustment.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAdjustmentSaveDto {
    
    private Integer transactionCode;    // 거래내역코드 (null이면 신규생성, 값이 있으면 수정)
    private Integer customerCode;       // 거래처코드
    private String adjustmentDate;      // 조정일자 (YYYYMMDD)
    private String transactionType;     // 거래유형 (조정 default)
    private Integer adjustmentAmount;   // 조정금액 (+, - 가능)
    private String note;                // 비고
}