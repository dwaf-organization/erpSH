package com.inc.sh.dto.customer.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDeleteReqDto {
    
    private List<Integer> customerCodes;  // 삭제할 거래처코드 배열
}