package com.inc.sh.dto.customerUser.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [앱전용] 로그인 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppLoginReqDto {
    
    private String customerUserId;    // 사용자 아이디
    private String customerUserPw;    // 사용자 비밀번호
}