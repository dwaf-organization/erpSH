package com.inc.sh.dto.role.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSaveDto {
    
    private Integer roleCode;       // 권한코드 (null이면 신규생성, 값이 있으면 수정)
    private String roleName;        // 권한이름
    private String note;            // 세부내용
    private Integer hqCode;         // 본사코드
}