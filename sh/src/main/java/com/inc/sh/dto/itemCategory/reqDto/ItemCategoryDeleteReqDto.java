package com.inc.sh.dto.itemCategory.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryDeleteReqDto {
    
    private List<Integer> categoryCodes;  // 삭제할 품목분류코드 배열
}