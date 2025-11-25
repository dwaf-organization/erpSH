package com.inc.sh.dto.itemCustomerPrice.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCustomerPriceUpdateRespDto {
    
    private Integer itemCode;
    private Integer processedCount; // 처리된 거래처 수
    private List<ProcessResult> processResults;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessResult {
        private Integer customerCode;
        private String action; // "CREATED", "UPDATED", "DELETED"
        private Integer customerPrice;
        private String message;
    }
}