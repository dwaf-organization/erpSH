package com.inc.sh.dto.signin.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSigninRespDto {
    
    private String userCode;           // 사원코드
    private Integer roleCode;           // 권한코드
    private String userName;            // 사원명
}