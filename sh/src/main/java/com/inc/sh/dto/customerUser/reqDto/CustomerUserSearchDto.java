package com.inc.sh.dto.customerUser.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserSearchDto {
    
    private Integer customerCode;       // 거래처코드 (완전일치 또는 전체)
    private String customerUserId;      // 사용자아이디 (부분검색)
}