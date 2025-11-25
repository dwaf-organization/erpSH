package com.inc.sh.dto.common.respDto;

import com.inc.sh.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSelectDto {
    
    private Integer value;       // 권한코드 (value)
    private String label;        // 권한명 (옵션값)
    
    /**
     * Entity to DTO 변환 (셀렉트박스용)
     */
    public static RoleSelectDto fromEntity(Role role) {
        return RoleSelectDto.builder()
                .value(role.getRoleCode())
                .label(role.getRoleName())
                .build();
    }
}