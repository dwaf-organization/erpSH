package com.inc.sh.dto.signin.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSigninDto {
    
    private Integer hqCode;             // 본사코드
    private String userCode;           // 사원코드
    private String userPw;              // 사원비밀번호
}