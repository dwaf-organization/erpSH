package com.inc.sh.dto.warehouse.reqDto;

import com.inc.sh.entity.Warehouse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseReqDto {
    
    // 수정시에만 사용 (null이면 신규 생성)
    private Integer warehouseCode;
    
    @NotBlank(message = "창고명은 필수입니다")
    private String warehouseName;
    
    @NotNull(message = "물류센터코드는 필수입니다")
    private Integer distCenterCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    private String zipCode;         // 우편번호
    private String addr;            // 주소
    private String telNum;          // 전화번호
    private String managerName;     // 담당자명
    private String managerContact;  // 담당자연락처
    private Integer useYn;          // 사용여부 (기본값 1)
    
    /**
     * DTO to Entity 변환 (신규 등록)
     */
    public Warehouse toEntity() {
        return Warehouse.builder()
                .warehouseName(this.warehouseName)
                .distCenterCode(this.distCenterCode)
                .hqCode(this.hqCode)
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
    public void updateEntity(Warehouse warehouse) {
        warehouse.setWarehouseName(this.warehouseName);
        warehouse.setDistCenterCode(this.distCenterCode);
        warehouse.setHqCode(this.hqCode);
        warehouse.setZipCode(this.zipCode);
        warehouse.setAddr(this.addr);
        warehouse.setTelNum(this.telNum);
        warehouse.setManagerName(this.managerName);
        warehouse.setManagerContact(this.managerContact);
        if (this.useYn != null) {
            warehouse.setUseYn(this.useYn);
        }
    }
}