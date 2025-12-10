package com.inc.sh.dto.distCenter.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterDeleteReqDto {
    
    private List<Integer> distCenterCodes;  // 삭제할 물류센터코드 배열
}