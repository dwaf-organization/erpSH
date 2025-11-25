package com.inc.sh.dto.virtualAccount.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountDeleteRespDto {
    
    private Integer virtualAccountCode;
    private Integer linkedCustomerCode; // 연결된 거래처코드 (있을 경우 반환)
    private String message;
}