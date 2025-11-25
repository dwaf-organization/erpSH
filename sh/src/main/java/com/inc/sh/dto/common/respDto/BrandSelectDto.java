package com.inc.sh.dto.common.respDto;

import com.inc.sh.entity.BrandInfo;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandSelectDto {
    
    private Integer value;       // 브랜드코드 (value)
    private String label;        // 브랜드명 (옵션값)
    
    /**
     * Entity to DTO 변환 (셀렉트박스용)
     */
    public static BrandSelectDto fromEntity(BrandInfo brandInfo) {
        return BrandSelectDto.builder()
                .value(brandInfo.getBrandCode())
                .label(brandInfo.getBrandName())
                .build();
    }
}