package com.inc.sh.dto.distCenter.respDto;

import com.inc.sh.entity.DistCenter;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterRespDto {
    
    private Integer distCenterCode;
    private Integer hqCode;
    private String distCenterName;
    private String zipCode;
    private String addr;
    private String telNum;
    private String managerName;
    private String managerContact;
    private Integer useYn;
    
    /**
     * Entity to DTO 변환
     */
    public static DistCenterRespDto fromEntity(DistCenter distCenter) {
        return DistCenterRespDto.builder()
                .distCenterCode(distCenter.getDistCenterCode())
                .hqCode(distCenter.getHqCode())
                .distCenterName(distCenter.getDistCenterName())
                .zipCode(distCenter.getZipCode())
                .addr(distCenter.getAddr())
                .telNum(distCenter.getTelNum())
                .managerName(distCenter.getManagerName())
                .managerContact(distCenter.getManagerContact())
                .useYn(distCenter.getUseYn())
                .build();
    }
}