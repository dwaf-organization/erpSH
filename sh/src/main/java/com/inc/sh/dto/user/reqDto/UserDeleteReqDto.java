package com.inc.sh.dto.user.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeleteReqDto {
    
    private List<String> userCodes;    // 삭제할 사번 배열
}