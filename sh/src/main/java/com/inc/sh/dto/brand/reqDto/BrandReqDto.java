package com.inc.sh.dto.brand.reqDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandReqDto {
    
    private Integer brandCode;
    
    @NotNull(message = "본사코드는 필수입니다")
    private Integer hqCode;
    
    @NotBlank(message = "브랜드명은 필수입니다")
    private String brandName;
    
    private String note;
}