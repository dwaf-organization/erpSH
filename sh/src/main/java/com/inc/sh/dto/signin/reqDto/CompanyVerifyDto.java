package com.inc.sh.dto.signin.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerifyDto {
    
    private String hqAccessCode;        // 본사접속코드
}