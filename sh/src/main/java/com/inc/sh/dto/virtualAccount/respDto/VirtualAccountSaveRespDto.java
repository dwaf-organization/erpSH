package com.inc.sh.dto.virtualAccount.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountSaveRespDto {
    
    private Integer virtualAccountCode;
    private Integer linkedCustomerCode; // 거래처코드 추가
}