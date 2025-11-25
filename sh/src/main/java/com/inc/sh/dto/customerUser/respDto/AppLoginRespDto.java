package com.inc.sh.dto.customerUser.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * [앱전용] 로그인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppLoginRespDto {
    
    private Integer customerUserCode;     // 사용자 코드
    private Integer customerCode;         // 고객 코드  
    private String customerUserName;      // 사용자 이름
    private Integer virtualAccountCode;   // 가상계좌 코드
}