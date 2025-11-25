package com.inc.sh.dto.popup.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSearchPopupDto {
    
    private String customerCode;    // 거래처코드 (부분일치)
    private String customerName;    // 거래처명 (부분일치)
    private Integer brandCode;      // 브랜드코드 (완전일치)
}
