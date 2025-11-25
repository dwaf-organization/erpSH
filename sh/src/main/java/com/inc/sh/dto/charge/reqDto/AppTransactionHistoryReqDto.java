package com.inc.sh.dto.charge.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppTransactionHistoryReqDto {
    
    private Integer customerCode;           // 거래처코드
    private String startTransactionDate;    // 시작 거래일자 (yyyyMMdd)
    private String endTransactionDate;      // 종료 거래일자 (yyyyMMdd)
    private Integer page;                   // 페이지 번호 (0부터 시작)
    private Integer size;                   // 페이지 크기 (기본 10개)
}