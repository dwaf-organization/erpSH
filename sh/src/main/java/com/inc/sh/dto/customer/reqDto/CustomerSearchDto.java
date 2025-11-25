package com.inc.sh.dto.customer.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSearchDto {
    
    private Integer brandCode;        // 브랜드코드 (완전일치)
    private String customerName;      // 상호 (부분일치)
    private Integer closeDtYn;        // 종료여부 (1=종료, 0=활성, null=전체)
    private Integer orderBlockYn;     // 주문금지여부 (1=금지, 0=허용, null=전체)
}