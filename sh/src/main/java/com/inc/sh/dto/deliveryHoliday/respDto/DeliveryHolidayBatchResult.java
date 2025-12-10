package com.inc.sh.dto.deliveryHoliday.respDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryHolidayBatchResult {
    
    private int totalCount;                                 // 총 처리 건수
    private int successCount;                               // 성공 건수
    private int failCount;                                  // 실패 건수
    private List<DeliveryHolidayDeleteRespDto> successData; // 성공한 배송휴일 데이터들
    private List<DeliveryHolidayErrorDto> failData;         // 실패한 배송휴일 데이터들
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DeliveryHolidayErrorDto {
        private Integer deliveryHolidayCode;                // 실패한 배송휴일코드
        private String holidayName;                         // 실패한 휴일명  
        private String errorMessage;                        // 실패 사유
    }
}