package com.inc.sh.dto.user.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchDto {
    
    private Integer hqCode;         // ✅ 본사코드 (완전일치, 필수)
    private String userCode;        // 사번 (부분검색)
    private String userName;        // 성명 (부분검색)
}