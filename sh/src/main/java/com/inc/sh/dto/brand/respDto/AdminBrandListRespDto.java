package com.inc.sh.dto.brand.respDto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminBrandListRespDto {
    
    private Integer brandCode;          // 브랜드코드
    private String brandName;           // 브랜드명
    private Integer hqCode;             // 본사코드
    private String hqName;              // 본사명 (companyName)
    private String note;                // 비고
}