package com.inc.sh.dto.returnManagement.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnDeleteReqDto {
    
    private List<String> returnNos;  // 삭제할 반품번호 배열
}