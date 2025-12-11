package com.inc.sh.dto.customerDepositStatus.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositStatusSearchDto {
    
    private String startDate;           // 시작일자 (YYYYMMDD)
    private String endDate;             // 종료일자 (YYYYMMDD)
    private Integer customerCode;       // 거래처코드 (선택, 완전일치, null이면 전체)
    private Integer brandCode;          // 브랜드코드 (선택, 완전일치, null이면 전체)
    private Integer depositMethod;       // 입금유형 (0=일반입금, 1=가상계좌, null이면 전체)
    private Integer hqCode;             // 본사코드
}