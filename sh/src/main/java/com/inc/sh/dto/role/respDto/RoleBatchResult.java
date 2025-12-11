package com.inc.sh.dto.role.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleBatchResult {
    
    private int totalCount;                               // 총 처리 건수
    private int successCount;                             // 성공 건수
    private int failureCount;                             // 실패 건수
    
    private List<RoleSuccessResult> successList;          // 성공 목록
    private List<RoleFailureResult> failureList;          // 실패 목록
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleSuccessResult {
        private Integer roleCode;                         // 권한코드
        private String roleName;                          // 권한이름
        private String note;                              // 세부내용
        private Integer hqCode;                           // 본사코드
        private String message;                           // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoleFailureResult {
        private Integer roleCode;                         // 권한코드 (null 가능)
        private String roleName;                          // 권한이름 (null 가능)
        private String reason;                            // 실패 원인
    }
}