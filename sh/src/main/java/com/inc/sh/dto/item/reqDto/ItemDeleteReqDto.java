package com.inc.sh.dto.item.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDeleteReqDto {
    
    private List<Integer> itemCodes;  // 삭제할 품목코드 배열
}