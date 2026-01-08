package com.inc.sh.dto.platform.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HyphenOrderReqDto {
    
    private String userId;      // 배민 로그인 ID
    private String userPw;      // 배민 로그인 PW
    private String dateFrom;    // 조회시작일 (YYYY-MM-DD)
    private String dateTo;      // 조회종료일 (YYYY-MM-DD)
    private String processYn;   // 처리여부 (Y/N)
    private String detailYn;    // 상세여부 (Y/N)
}