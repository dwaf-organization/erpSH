package com.inc.sh.dto.returnManagement.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnSearchDto {
    
    private String startDate;           // 반품일자 시작일 (YYYYMMDD)
    private String endDate;             // 반품일자 종료일 (YYYYMMDD)
    private Integer customerCode;       // 거래처코드 (완전일치)
    private String status;              // 진행상태 (전체=null, 승인, 미승인)
    private Integer hqCode;             // 본사코드
}