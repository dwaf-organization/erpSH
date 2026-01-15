package com.inc.sh.dto.common.respDto;

import com.inc.sh.entity.Headquarter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HqSelectDto {
    private Integer value;       // 브랜드코드 (value)
    private String label;        // 브랜드명 (옵션값)
    
    /**
     * Entity to DTO 변환 (셀렉트박스용)
     */
    public static HqSelectDto fromEntity(Headquarter headquarter) {
        return HqSelectDto.builder()
                .value(headquarter.getHqCode())
                .label(headquarter.getCompanyName())
                .build();
    }
}
