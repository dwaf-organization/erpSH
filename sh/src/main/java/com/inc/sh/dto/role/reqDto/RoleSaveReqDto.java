package com.inc.sh.dto.role.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSaveReqDto {
    
    private List<RoleSaveItemDto> roles;    // 권한 배열
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleSaveItemDto {
        private Integer roleCode;       // 권한코드 (null이면 신규생성, 값이 있으면 수정)
        private String roleName;        // 권한이름
        private String note;            // 세부내용
        private Integer hqCode;         // 본사코드
    }
}