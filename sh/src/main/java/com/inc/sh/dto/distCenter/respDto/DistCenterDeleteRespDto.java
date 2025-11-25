package com.inc.sh.dto.distCenter.respDto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterDeleteRespDto {
    
    private Integer distCenterCode;
    private List<Integer> relatedWarehouseCodes; // 연관된 창고코드들
    private String message;
}