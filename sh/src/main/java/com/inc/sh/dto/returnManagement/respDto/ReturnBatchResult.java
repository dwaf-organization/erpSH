package com.inc.sh.dto.returnManagement.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<ReturnRespDto> successData;            // 성공한 반품 데이터들
    private List<ReturnErrorDto> failData;              // 실패한 반품 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnErrorDto {
        private String returnNo;                        // 실패한 반품번호
        private String customerName;                    // 실패한 거래처명  
        private String itemName;                        // 실패한 품목명
        private String errorMessage;                    // 실패 사유
    }
}