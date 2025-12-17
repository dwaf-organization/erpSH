package com.inc.sh.dto.user.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSaveReqDto {
    
    private List<UserSaveItemDto> users;    // 사용자 배열
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSaveItemDto {
        private String userCode;        // 사번 (null이면 신규생성, 값이 있으면 수정)
        private String userName;        // 성명
        private String email;           // 이메일
        private String phone1;          // 연락처1
        private String phone2;          // 연락처2
        private Integer hqCode;         // 본사코드
        private Integer roleCode;       // 사용자권한코드
        private String resignationDt;   // 퇴사일자 (YYYYMMDD)
        private String userPw;          // 암호
        private Integer ledgerUsageYn;  // 장부대장 사용여부 (0=사용불가, 1=사용가능)
    }
}