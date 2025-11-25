package com.inc.sh.dto.virtualAccount.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountSearchDto {
    
    private Integer linkedCustomerCode;     // 연결거래처코드 (완전일치)
    private String virtualAccountStatus;    // 가상계좌상태 ("전체", "사용", "미사용")
    private String closeDtYn;               // 해지여부 ("전체", "Y", "N")
}