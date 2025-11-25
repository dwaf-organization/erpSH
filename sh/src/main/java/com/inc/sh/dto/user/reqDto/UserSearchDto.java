package com.inc.sh.dto.user.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchDto {
    
    private String userCode;        // 사번 (부분검색)
    private String userName;        // 성명 (부분검색)
}