package com.inc.sh.dto.signin.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyVerifyRespDto {
    
    private Integer hqCode;             // 본사코드
    private String hqAccessCode;        // 본사접속코드
    private String hqName;              // 본사명
}