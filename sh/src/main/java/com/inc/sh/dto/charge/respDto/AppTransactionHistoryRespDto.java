package com.inc.sh.dto.charge.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTransactionHistoryRespDto {
    
    private AppTransactionSummaryRespDto summary;           // 거래내역 합계
    private Integer currentPage;                            // 현재 페이지
    private Integer totalPages;                             // 총 페이지 수
    private Boolean hasNext;                                // 다음 페이지 여부
    private List<AppTransactionRespDto> transactions;       // 거래내역 목록
}