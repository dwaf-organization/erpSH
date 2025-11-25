package com.inc.sh.dto.returnRegistration.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnRequestRespDto {
    
    private String returnNo;           // 생성된 반품번호
    private String returnMessage;      // 성공 메시지
}