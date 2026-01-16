package com.inc.sh.dto.user.reqDto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminPasswordResetReqDto {
    
    /**
     * 초기화할 사용자 코드
     */
    @NotBlank(message = "사용자 코드는 필수입니다")
    private String userCode;
}