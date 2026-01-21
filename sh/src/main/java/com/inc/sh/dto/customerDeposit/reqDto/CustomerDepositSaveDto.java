package com.inc.sh.dto.customerDeposit.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositSaveDto {
    
    private List<CustomerDepositItemDto> deposits;  // 입금 배열
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomerDepositItemDto {
        private Integer depositId;              // null=신규, 값=수정
        private Integer customerCode;           // 거래처코드 (필수)
        private Integer depositMethod;           // 입금방법 (0=후입금, 1=가상계좌) (필수)
        private String depositorName;           // 입금자명
        private String depositDate;             // 입금일자 (YYYYMMDD) (필수)
        private Integer depositAmount;          // 입금금액 (필수)
        private String note;                    // 비고
        private String referenceId;             // 참조코드 (주문번호, 반품번호 등)
    }
}