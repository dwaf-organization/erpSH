package com.inc.sh.dto.popup.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountSearchPopupDto {
    
    private Integer hqCode;                 // 본사코드 (필수)
    private String virtualAccountNum;       // 가상계좌번호 (부분일치) - 실제 컬럼명 맞춤
    private String virtualAccountStatus;    // 가상계좌상태 (완전일치)
}