package com.inc.sh.dto.customerCollectionStatus.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCollectionStatusSearchDto {
    
    private String startDate;           // 시작일자 (YYYYMMDD)
    private String endDate;             // 종료일자 (YYYYMMDD)
    private Integer customerCode;       // 거래처코드 (선택, 완전일치, null이면 전체)
    private Integer hqCode;             // 본사코드
}