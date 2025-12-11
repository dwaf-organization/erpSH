package com.inc.sh.dto.customerUser.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserBatchResult {
    
    private int totalCount;                                     // 총 처리 건수
    private int successCount;                                   // 성공 건수
    private int failureCount;                                   // 실패 건수
    
    private List<CustomerUserSuccessResult> successList;        // 성공 목록
    private List<CustomerUserFailureResult> failureList;        // 실패 목록
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerUserSuccessResult {
        private Integer customerUserCode;                       // 사용자코드
        private Integer customerCode;                           // 거래처코드
        private String customerName;                            // 거래처명
        private String customerUserId;                          // 사용자아이디
        private String customerUserName;                        // 사용자명
        private Integer endYn;                                  // 종료여부
        private String message;                                 // 처리 메시지
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerUserFailureResult {
        private Integer customerUserCode;                       // 사용자코드 (null 가능)
        private Integer customerCode;                           // 거래처코드 (null 가능)
        private String customerUserId;                          // 사용자아이디 (null 가능)
        private String reason;                                  // 실패 원인
    }
}