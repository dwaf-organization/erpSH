package com.inc.sh.dto.common.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCategorySelectDto {
    
    private Integer value;       // 중분류코드 (value)
    private String label;        // "대분류명-중분류명" (옵션값)
    
    /**
     * 대분류명과 중분류명을 조합한 DTO 생성
     * @param categoryCode 중분류코드
     * @param majorCategoryName 대분류명
     * @param subCategoryName 중분류명
     * @return ItemCategorySelectDto
     */
    public static ItemCategorySelectDto of(Integer categoryCode, String majorCategoryName, String subCategoryName) {
        return ItemCategorySelectDto.builder()
                .value(categoryCode)
                .label(majorCategoryName + "-" + subCategoryName)
                .build();
    }
}