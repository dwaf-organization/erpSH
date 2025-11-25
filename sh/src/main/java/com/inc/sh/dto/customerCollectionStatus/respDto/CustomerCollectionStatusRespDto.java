package com.inc.sh.dto.customerCollectionStatus.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCollectionStatusRespDto {
    
    private Integer customerCode;       // 거래처코드
    private String customerName;        // 거래처명
    private Integer creditLimit;        // 여신한도
    private Integer previousBalance;    // 전일잔액 (시작일 이전 최근 잔액)
    private Integer salesAmount;        // 매출액 (출금+외상-반품입금)
    private Integer depositAmount;      // 입금액
    private Integer adjustmentAmount;   // 조정액
    private Integer currentBalance;     // 잔액 (수식으로 계산)
}