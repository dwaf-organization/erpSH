package com.inc.sh.dto.customerDeposit.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositSaveDto {
    
    private Integer depositId;          // 입금코드 (null이면 신규생성, 값이 있으면 수정)
    private Integer customerCode;       // 거래처코드
    private String depositMethod;       // 입금유형 (0=일반입금, 1=가상계좌)
    private String depositorName;       // 입금자명
    private String depositDate;         // 입금일자 (YYYYMMDD)
    private Integer depositAmount;      // 입금금액
    private String note;                // 비고
}