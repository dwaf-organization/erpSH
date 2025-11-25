package com.inc.sh.dto.itemCategory.respDto;

import com.inc.sh.entity.ItemCategory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryTableRespDto {
    
    private Integer categoryCode;
    private Integer hqCode;
    private Integer parentsCategoryCode;
    private String categoryName;
    private Integer categoryLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity to Table DTO 변환
     */
    public static ItemCategoryTableRespDto fromEntity(ItemCategory itemCategory) {
        return ItemCategoryTableRespDto.builder()
                .categoryCode(itemCategory.getCategoryCode())
                .hqCode(itemCategory.getHqCode())
                .parentsCategoryCode(itemCategory.getParentsCategoryCode())
                .categoryName(itemCategory.getCategoryName())
                .categoryLevel(itemCategory.getCategoryLevel())
                .createdAt(itemCategory.getCreatedAt())
                .updatedAt(itemCategory.getUpdatedAt())
                .build();
    }
}