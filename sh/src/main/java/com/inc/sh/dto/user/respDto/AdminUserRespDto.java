package com.inc.sh.dto.user.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserRespDto {
    
    private String userCode;            // 사용자번호
    private String userName;            // 사용자명
    private Integer hqCode;             // 본사코드
    private String hqName;              // 본사명
    private Integer roleCode;           // 권한코드
    private String roleName;            // 권한명
    private String phone1;              // 연락처1
    private String email;               // 이메일
}