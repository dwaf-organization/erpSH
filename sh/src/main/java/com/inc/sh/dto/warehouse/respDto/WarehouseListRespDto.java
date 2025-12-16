package com.inc.sh.dto.warehouse.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseListRespDto {

    private Integer warehouseCode;        // 창고코드
    private Integer distCenterCode;       // 물류센터코드
    private Integer hqCode;               // 본사코드
    private String warehouseName;         // 창고명
    private String distCenterName;        // 물류센터명 (JOIN 결과)
    private String zipCode;               // 우편번호
    private String addr;                  // 주소
    private String telNum;                // 전화번호
    private String managerName;           // 담당자명
    private String managerContact;        // 담당자연락처
    private Integer useYn;                // 사용여부


    /**
     * Object[] 배열에서 생성 (SELECT * 전체 조회 결과)
     * Warehouse Entity 전체 필드 + JOIN된 dist_center_name
     */
    public static WarehouseListRespDto of(Object[] result) {
        return WarehouseListRespDto.builder()
                .warehouseCode((Integer) result[0])        // warehouse_code
                .distCenterCode((Integer) result[1])       // dist_center_code
                .hqCode((Integer) result[2])               // hq_code
                .warehouseName((String) result[3])         // warehouse_name
                .zipCode((String) result[4])               // zip_code
                .addr((String) result[5])                  // addr
                .telNum((String) result[6])                // tel_num
                .managerName((String) result[7])           // manager_name
                .managerContact((String) result[8])        // manager_contact
                .useYn((Integer) result[9])                // use_yn
                .distCenterName((String) result[13])       // dist_center_name (JOIN)
                .build();
    }
}