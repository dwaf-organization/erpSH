package com.inc.sh.dto.customerDeposit.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositSearchDto {
    
    private Integer customerCode;       // 거래처코드 (선택, 완전일치)
    private String startDate;           // 시작일자 (YYYYMMDD)
    private String endDate;             // 종료일자 (YYYYMMDD)
    private Integer depositTypeCode;    // 입금유형 (0=후입금, 1=충전형)
}