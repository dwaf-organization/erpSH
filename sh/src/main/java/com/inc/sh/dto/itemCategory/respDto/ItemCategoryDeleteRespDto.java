package com.inc.sh.dto.itemCategory.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryDeleteRespDto {
    
    private Integer categoryCode;
    private List<Integer> relatedItemCodes; // 연관된 품목코드들
    private String message;
}