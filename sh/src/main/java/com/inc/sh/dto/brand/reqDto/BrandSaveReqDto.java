package com.inc.sh.dto.brand.reqDto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandSaveReqDto {
    
    private List<BrandItemDto> brands;  // 브랜드 배열
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BrandItemDto {
        private Integer brandCode;      // null=CREATE, 값=UPDATE
        private Integer hqCode;         // 본사코드
        private String brandName;       // 브랜드명 (필수)
        private String note;           // 비고 (선택)
    }
}