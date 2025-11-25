package com.inc.sh.dto.role.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSearchDto {
    
    private String roleCode;        // 권한코드 (부분검색)
    private String roleName;        // 권한이름 (부분검색)
}