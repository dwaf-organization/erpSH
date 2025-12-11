package com.inc.sh.dto.customerUser.reqDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUserDeleteReqDto {
    
    private List<Integer> customerUserCodes;    // 삭제할 사용자코드 배열
}