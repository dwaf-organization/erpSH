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
public class ItemCategorySaveReqDto {
    
    private List<ItemCategoryItemDto> itemCategories;  // 품목분류 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ItemCategoryItemDto {
        private Integer categoryCode;           // null=CREATE, 값=UPDATE
        private Integer hqCode;                // 본사코드 (필수)
        private Integer parentsCategoryCode;    // 부모분류코드 (0=대분류)
        private String categoryName;           // 분류명 (필수)
        private Integer categoryLevel;         // 분류레벨 (1=대분류, 2=중분류)
        private String description;           // 설명 (선택)
    }
}