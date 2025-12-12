package com.inc.sh.dto.user.reqDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserReqDto {
    
    private String userCode;            // null: 신규생성, 값있음: 수정
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;             // 본사코드
    
    @NotBlank(message = "사용자명은 필수입니다")
    private String userName;            // 사용자명
    
    private String userPw;              // 비밀번호 (신규시 필수, 수정시 선택)
    
    private String phone1;              // 연락처1
    
    private String email;               // 이메일
}