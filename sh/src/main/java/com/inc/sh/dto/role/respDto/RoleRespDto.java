package com.inc.sh.dto.role.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRespDto {
    
    private Integer roleCode;       // 권한코드
    private String roleName;        // 권한이름
    private String note;            // 세부내용
    private Integer hqCode;         // 본사코드 (수정 시 필요)
}