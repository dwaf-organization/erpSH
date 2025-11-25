package com.inc.sh.dto.charge.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTransactionSummaryRespDto {
    
    private Integer totalDeposit;       // 총 입금액
    private Integer totalOrder;         // 총 출금액 (절댓값)
    private Integer totalAdjustment;    // 총 조정액
    private Integer totalReturn;        // 총 반품입금액
    private Long totalCount;            // 총 거래건수
}