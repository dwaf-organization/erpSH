package com.inc.sh.dto.distCenter.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterBatchResult {
    
    private int totalCount;                             // 총 처리 건수
    private int successCount;                           // 성공 건수
    private int failCount;                              // 실패 건수
    private List<DistCenterRespDto> successData;        // 성공한 물류센터 데이터들
    private List<DistCenterErrorDto> failData;          // 실패한 물류센터 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistCenterErrorDto {
        private Integer distCenterCode;                 // 실패한 물류센터코드
        private String distCenterName;                  // 실패한 물류센터명  
        private String errorMessage;                    // 실패 사유
    }
}