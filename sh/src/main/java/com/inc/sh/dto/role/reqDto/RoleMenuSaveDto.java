package com.inc.sh.dto.role.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleMenuSaveDto {
    
    private Integer roleCode;           // 권한코드
    private List<Integer> menuCodes;    // 선택된 메뉴코드 리스트
}