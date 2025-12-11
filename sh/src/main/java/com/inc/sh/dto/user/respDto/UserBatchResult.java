package com.inc.sh.dto.user.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBatchResult {
    
    private int totalCount;                            // 총 처리 건수
    private int successCount;                          // 성공 건수
    private int failureCount;                          // 실패 건수
    
    private List<UserSuccessResult> successList;       // 성공 목록
    private List<UserFailureResult> failureList;       // 실패 목록
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserSuccessResult {
        private String userCode;                       // 사번
        private String userName;                       // 성명
        private Integer hqCode;                        // 본사코드
        private Integer roleCode;                      // 권한코드
        private String roleName;                       // 권한명
        private String message;                        // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserFailureResult {
        private String userCode;                       // 사번 (null 가능)
        private String userName;                       // 성명 (null 가능)
        private String reason;                         // 실패 원인
    }
}