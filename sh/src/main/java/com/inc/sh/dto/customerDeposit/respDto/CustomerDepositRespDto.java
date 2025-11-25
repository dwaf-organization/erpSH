package com.inc.sh.dto.customerDeposit.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositRespDto {
    
    private Integer depositId;          // 입금코드
    private Integer customerCode;       // 거래처코드
    private String customerName;        // 거래처명
    private String depositDate;         // 입금일자 (YYYY-MM-DD)
    private Integer depositAmount;      // 입금금액
    private String depositMethod;       // 입금유형 (0=일반입금, 1=가상계좌)
    private String depositorName;       // 입금자명
    private String note;                // 비고
}