package com.inc.sh.dto.order.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [앱전용] 가상계좌 정보 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppAccountInfoRespDto {
    
    // 가상계좌 정보
    private String virtualAccountNum;     // 가상계좌번호
    private String bankName;              // 은행명
    private String virtualAccountStatus;  // 가상계좌 상태
    
    // 잔액 및 여신 정보
    private Integer balanceAmt;           // 현재 잔액
    private Integer creditLimit;          // 여신한도
}