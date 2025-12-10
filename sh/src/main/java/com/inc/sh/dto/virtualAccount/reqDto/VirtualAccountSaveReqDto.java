package com.inc.sh.dto.virtualAccount.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountSaveReqDto {
    
    private List<VirtualAccountItemDto> virtualAccounts;  // 가상계좌 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VirtualAccountItemDto {
        private Integer virtualAccountCode;      // null=CREATE, 값=UPDATE
        private Integer hqCode;                 // 본사코드
        private String virtualAccountNum;       // 가상계좌번호 (필수)
        private String virtualAccountStatus;    // 가상계좌상태 (필수)
        private String bankName;               // 은행명 (필수)
        private Integer linkedCustomerCode;     // 연결거래처코드 (선택)
        private String openDt;                 // 개설일자 (선택)
        private String note;                   // 비고 (선택)
    }
}