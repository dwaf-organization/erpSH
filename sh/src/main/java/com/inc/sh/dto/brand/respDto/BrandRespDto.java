package com.inc.sh.dto.brand.respDto;

import com.inc.sh.entity.BrandInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BrandRespDto {
    
    private Integer brandCode;
    private Integer hqCode;
    private String brandName;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> RespDto 변환
     */
    public static BrandRespDto from(BrandInfo entity) {
        return BrandRespDto.builder()
                .brandCode(entity.getBrandCode())
                .hqCode(entity.getHqCode())
                .brandName(entity.getBrandName())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}