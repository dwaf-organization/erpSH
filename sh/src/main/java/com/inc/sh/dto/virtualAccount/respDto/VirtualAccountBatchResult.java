package com.inc.sh.dto.virtualAccount.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountBatchResult {
    
    private int totalCount;                                     // 총 처리 건수
    private int successCount;                                   // 성공 건수
    private int failCount;                                      // 실패 건수
    private List<VirtualAccountRespDto> successData;            // 성공한 가상계좌 데이터들
    private List<VirtualAccountErrorDto> failData;              // 실패한 가상계좌 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VirtualAccountErrorDto {
        private Integer virtualAccountCode;                     // 실패한 가상계좌코드
        private String virtualAccountNum;                       // 실패한 가상계좌번호  
        private String errorMessage;                            // 실패 사유
    }
}