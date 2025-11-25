package com.inc.sh.dto.user.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRespDto {
    
    private String userCode;        // 사번
    private String userName;        // 성명
    private String phone1;          // 연락처1
    private String phone2;          // 연락처2
    private String email;           // 이메일
    private String resignationDt;   // 퇴사일자 (YYYY-MM-DD)
    private Integer roleCode;       // 사용자권한코드
    private String roleName;        // 사용자권한명
    private String userPw;          // 비밀번호 (조회 시 표시용)
}