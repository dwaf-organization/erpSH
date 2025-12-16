package com.inc.sh.dto.analysisTest.respDto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTestRespDto {
    
    private Integer districtCode;           // 구코드
    private String districtName;            // 구명
    private Long maxSales;                  // 구최고매출
    private Long minSales;                  // 구최저매출
    private Long currentSales;              // 현재구의매출
    private BigDecimal growthRate;          // 전월대비상승하락율(%)
    private Integer growthCode;             // 전월대비코드(1=상승, 0=하락)
    private String growthStatus;            // 상승/하락 텍스트
    private String peakTrafficHour;         // 유동인구가장많은시간대
    private String analysisDescription;     // 분석해설
    
    /**
     * Entity -> RespDto 변환 (정적 메서드)
     */
    public static AnalysisTestRespDto from(com.inc.sh.entity.DistrictAnalysis entity) {
        return AnalysisTestRespDto.builder()
                .districtCode(entity.getDistrictCode())
                .districtName(entity.getDistrictName())
                .maxSales(entity.getMaxSales())
                .minSales(entity.getMinSales())
                .currentSales(entity.getCurrentSales())
                .growthRate(entity.getGrowthRate())
                .growthCode(entity.getGrowthCode())
                .growthStatus(entity.getGrowthCode() == 1 ? "상승" : "하락")
                .peakTrafficHour(entity.getPeakTrafficHour())
                .analysisDescription(entity.getAnalysisDescription())
                .build();
    }
}