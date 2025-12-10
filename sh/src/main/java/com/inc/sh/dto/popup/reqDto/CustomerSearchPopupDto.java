package com.inc.sh.dto.popup.reqDto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSearchPopupDto {
    
    private Integer hqCode;         // 본사코드 (필수)
    private String customerSearch;  // 거래처코드 또는 거래처명 (통합 검색)
    private String brandCode;       // 브랜드코드 또는 '전체' (문자열로 받음)
}