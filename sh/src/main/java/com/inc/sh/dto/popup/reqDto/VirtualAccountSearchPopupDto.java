package com.inc.sh.dto.popup.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountSearchPopupDto {
    
    private String virtualAccountCode;  // 가상계좌코드 (부분일치)
    private String virtualAccount;      // 가상계좌번호 (부분일치)
    private String accountStatus;       // 계좌상태 (사용, 미사용)
}