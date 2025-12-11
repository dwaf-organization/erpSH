package com.inc.sh.dto.role.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDeleteReqDto {
    
    private List<Integer> roleCodes;    // 삭제할 권한코드 배열
}