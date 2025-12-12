package com.inc.sh.dto.user.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserListRespDto {
    
    private String userCode;            // 사용자번호
    private String userName;            // 사용자명
    private Integer hqCode;             // 본사코드
    private String hqName;              // 본사명 (companyName)
    private Integer roleCode;           // 권한코드
    private String roleName;            // 권한명
    private String phone1;              // 연락처1
    private String phone2;              // 연락처2
    private String email;               // 이메일
    private String workStatus;          // 사용여부 ("재직중" / "퇴사")
    private String resignationDt;       // 퇴사일자 (null이면 재직중)
}