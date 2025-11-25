package com.inc.sh.dto.customerUser.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserSaveDto {
    
    private Integer customerUserCode;       // 사용자코드 (null이면 신규생성, 값이 있으면 수정)
    private Integer customerCode;           // 거래처코드
    private String customerUserId;          // 사용자아이디
    private String customerUserName;        // 이름
    private String contactNum;              // 연락처
    private String email;                   // 이메일
    private Integer endYn;                  // 종료여부 (0=사용중, 1=종료)
    private String customerUserPw;          // 암호
}