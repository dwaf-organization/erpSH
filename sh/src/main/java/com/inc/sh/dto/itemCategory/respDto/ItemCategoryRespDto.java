package com.inc.sh.dto.itemCategory.respDto;

import com.inc.sh.entity.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryRespDto {
    
    private Integer categoryCode;
    private Integer hqCode;
    private Integer parentsCategoryCode;    // 0=대분류
    private String categoryName;
    private Integer categoryLevel;          // 1=대분류, 2=중분류
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity를 DTO로 변환
     */
    public static ItemCategoryRespDto fromEntity(ItemCategory itemCategory) {
        return ItemCategoryRespDto.builder()
                .categoryCode(itemCategory.getCategoryCode())
                .hqCode(itemCategory.getHqCode())
                .parentsCategoryCode(itemCategory.getParentsCategoryCode())
                .categoryName(itemCategory.getCategoryName())
                .categoryLevel(itemCategory.getCategoryLevel())
                .description(itemCategory.getDescription())
                .createdAt(itemCategory.getCreatedAt())
                .updatedAt(itemCategory.getUpdatedAt())
                .build();
    }
}