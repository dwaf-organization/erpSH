package com.inc.sh.dto.returnRegistration.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppReturnCancelRespDto {
    
    private String returnNo;           // 취소된 반품번호
    private String cancelMessage;      // 취소 완료 메시지
}