package com.inc.sh.dto.customerDeposit.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositBatchResult {
    
    private int totalCount;                                     // 총 처리 건수
    private int successCount;                                   // 성공 건수
    private int failCount;                                      // 실패 건수
    private List<CustomerDepositRespDto> successData;           // 성공한 입금 데이터들
    private List<CustomerDepositErrorDto> failData;             // 실패한 입금 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerDepositErrorDto {
        private Integer depositId;                              // 실패한 입금코드
        private Integer customerCode;                           // 실패한 거래처코드
        private String customerName;                            // 실패한 거래처명
        private String errorMessage;                            // 실패 사유
    }
}