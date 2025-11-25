package com.inc.sh.dto.distCenter.reqDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterSearchDto {
    
    private Integer distCenterCode; // 물류센터코드 (완전일치)
    private Integer useYn;          // 사용여부 (0=미사용, 1=사용)
}