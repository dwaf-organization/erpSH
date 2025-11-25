package com.inc.sh.dto.itemCategory.respDto;

import com.inc.sh.entity.ItemCategory;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryTreeRespDto {
    
    private Integer categoryCode;
    private Integer parentsCategoryCode;
    private String categoryName;
    private Integer categoryLevel;
    private List<ItemCategoryTreeRespDto> children; // 하위 분류 목록
    
    /**
     * Entity to Tree DTO 변환 (하위 분류 제외)
     */
    public static ItemCategoryTreeRespDto fromEntity(ItemCategory itemCategory) {
        return ItemCategoryTreeRespDto.builder()
                .categoryCode(itemCategory.getCategoryCode())
                .parentsCategoryCode(itemCategory.getParentsCategoryCode())
                .categoryName(itemCategory.getCategoryName())
                .categoryLevel(itemCategory.getCategoryLevel())
                .build();
    }
}