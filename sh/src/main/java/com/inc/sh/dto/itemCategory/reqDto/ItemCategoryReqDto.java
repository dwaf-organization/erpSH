package com.inc.sh.dto.itemCategory.reqDto;

import com.inc.sh.entity.ItemCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategoryReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer categoryCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotNull(message = "상위분류코드는 필수입니다")
    private Integer parentsCategoryCode; // 0=대분류
    
    @NotBlank(message = "분류명은 필수입니다")
    private String categoryName;
    
    @NotNull(message = "분류순서는 필수입니다")
    private Integer categoryLevel;
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public ItemCategory toEntity() {
        return ItemCategory.builder()
                .hqCode(this.hqCode)
                .parentsCategoryCode(this.parentsCategoryCode)
                .categoryName(this.categoryName)
                .categoryLevel(this.categoryLevel)
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(ItemCategory itemCategory) {
        itemCategory.setHqCode(this.hqCode);
        itemCategory.setParentsCategoryCode(this.parentsCategoryCode);
        itemCategory.setCategoryName(this.categoryName);
        itemCategory.setCategoryLevel(this.categoryLevel);
    }
}