package com.inc.sh.dto.app.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [앱전용] 메인페이지 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppMainRespDto {
    
    // 거래처 기본 정보
    private String customerName;        // 거래처명
    private String ownerName;           // 대표자명
    private Integer balanceAmt;         // 현재 잔액
    
    // 최근 활동 정보
    private String recentOrderNo;       // 최근 주문번호
    private Integer recentOrderAmt;     // 최근 주문금액
    private Integer recentReturnAmt;    // 최근 반품요청금액 (없으면 null)
    private Integer recentDepositAmt;   // 최근 입금금액 (없으면 null)
}