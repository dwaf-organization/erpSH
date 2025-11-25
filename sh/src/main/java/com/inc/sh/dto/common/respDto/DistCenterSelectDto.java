package com.inc.sh.dto.common.respDto;

import com.inc.sh.entity.DistCenter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterSelectDto {
    
    private Integer value;       // 물류센터코드 (value)
    private String label;        // 물류센터명 (옵션값)
    
    /**
     * Entity to DTO 변환 (셀렉트박스용)
     */
    public static DistCenterSelectDto fromEntity(DistCenter distCenter) {
        return DistCenterSelectDto.builder()
                .value(distCenter.getDistCenterCode())
                .label(distCenter.getDistCenterName())
                .build();
    }
}