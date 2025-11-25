package com.inc.sh.dto.returnRegistration.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnHistoryReqDto {
    
    private Integer customerCode;      // 거래처코드
    private String startDate;          // 시작일 (yyyyMMdd)
    private String endDate;            // 종료일 (yyyyMMdd)
    private Integer page;              // 페이지 번호 (0부터 시작)
    private Integer size;              // 페이지 크기 (기본 10개)
}