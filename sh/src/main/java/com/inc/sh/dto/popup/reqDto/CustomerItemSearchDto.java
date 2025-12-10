package com.inc.sh.dto.popup.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerItemSearchDto {
    
    private Integer hqCode;           // 본사코드 (완전일치, 필수)
    private Integer customerCode;     // 거래처코드 (완전일치, 필수)
    private String item;              // 품목코드/품명 (부분일치, 선택)
    private Integer warehouseCode;    // 창고코드 (완전일치, 빈값가능)
    private Integer categoryCode;     // 분류코드 (완전일치, 빈값가능)
    private Integer priceType;        // 가격유형 (완전일치, 필수: 0or1)
}