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
public class DistCenterSaveReqDto {
    
    private List<DistCenterItemDto> distCenters;  // 물류센터 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistCenterItemDto {
        private Integer distCenterCode;         // null=CREATE, 값=UPDATE
        private Integer hqCode;                 // 본사코드 (필수)
        private String distCenterName;          // 물류센터명 (필수)
        private String zipCode;                 // 우편번호
        private String addr;                    // 주소
        private String telNum;                  // 전화번호
        private String managerName;             // 담당자명
        private String managerContact;          // 담당자연락처
        private Integer useYn;                  // 사용여부 (0=미사용, 1=사용)
        private String description;             // 설명
    }
}