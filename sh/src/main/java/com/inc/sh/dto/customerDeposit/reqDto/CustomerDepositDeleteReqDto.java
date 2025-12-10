package com.inc.sh.dto.customerDeposit.reqDto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDepositDeleteReqDto {
    
    private List<Integer> depositIds;  // 삭제할 입금코드 배열
}