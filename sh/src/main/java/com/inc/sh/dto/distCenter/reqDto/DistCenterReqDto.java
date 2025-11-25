package com.inc.sh.dto.distCenter.reqDto;

import com.inc.sh.entity.DistCenter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistCenterReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer distCenterCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotBlank(message = "물류센터명은 필수입니다")
    private String distCenterName;
    
    private String zipCode;         // 우편번호
    private String addr;            // 주소
    private String telNum;          // 전화번호
    private String managerName;     // 담당자명
    private String managerContact;  // 담당자연락처
    
    private Integer useYn;          // 사용여부 (기본값 1)
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public DistCenter toEntity() {
        return DistCenter.builder()
                .hqCode(this.hqCode)
                .distCenterName(this.distCenterName)
                .zipCode(this.zipCode)
                .addr(this.addr)
                .telNum(this.telNum)
                .managerName(this.managerName)
                .managerContact(this.managerContact)
                .useYn(this.useYn != null ? this.useYn : 1)
                .build();
    }
    
    /**
     * 기존 Entity 업데이트
     */
    public void updateEntity(DistCenter distCenter) {
        distCenter.setHqCode(this.hqCode);
        distCenter.setDistCenterName(this.distCenterName);
        distCenter.setZipCode(this.zipCode);
        distCenter.setAddr(this.addr);
        distCenter.setTelNum(this.telNum);
        distCenter.setManagerName(this.managerName);
        distCenter.setManagerContact(this.managerContact);
        if (this.useYn != null) {
            distCenter.setUseYn(this.useYn);
        }
    }
}