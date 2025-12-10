package com.inc.sh.dto.virtualAccount.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountDeleteReqDto {
    
    private List<Integer> virtualAccountCodes;  // 삭제할 가상계좌코드 배열
}