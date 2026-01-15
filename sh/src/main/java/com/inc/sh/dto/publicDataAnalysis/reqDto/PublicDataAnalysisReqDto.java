package com.inc.sh.dto.publicDataAnalysis.reqDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * 공공데이터 분석 요청 DTO 클래스들
 */
public class PublicDataAnalysisReqDto {

    /**
     * 매출 데이터 수집 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesDataCollectReq {
        
        @NotEmpty(message = "광역시 목록은 필수입니다.")
        private List<String> cities;
        
        @Builder.Default
        private Boolean applyMultiplier = true; // 배수 적용 여부
        
        @Builder.Default
        private Integer retryCount = 3; // 재시도 횟수
        
        @Builder.Default
        private Long delayMs = 100L; // API 호출 간 지연 시간 (ms)
    }

    /**
     * 소득/소비 데이터 수집 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeConsumptionDataCollectReq {
        
        @NotEmpty(message = "광역시 목록은 필수입니다.")
        private List<String> cities;
        
        @Builder.Default
        private Integer retryCount = 3; // 재시도 횟수
        
        @Builder.Default
        private Long delayMs = 100L; // API 호출 간 지연 시간 (ms)
    }

    /**
     * 업소수/인구 데이터 수집 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BusinessPopulationDataCollectReq {
        
        @NotEmpty(message = "지역코드 목록은 필수입니다.")
        private List<String> areaCodes;
        
        @Builder.Default
        private Boolean includeBusinessCount = true; // 업소수 포함 여부
        
        @Builder.Default
        private Boolean includePopulationData = true; // 인구 데이터 포함 여부
        
        @Builder.Default
        private Integer retryCount = 3; // 재시도 횟수
        
        @Builder.Default
        private Long delayMs = 100L; // API 호출 간 지연 시간 (ms)
    }

    /**
     * 통합 데이터 수집 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntegratedDataCollectReq {
        
        @NotEmpty(message = "광역시 목록은 필수입니다.")
        private List<String> cities;
        
        @NotEmpty(message = "지역코드 목록은 필수입니다.")
        private List<String> areaCodes;
        
        @Builder.Default
        private Boolean includeSales = true; // 매출 데이터 포함 여부
        
        @Builder.Default
        private Boolean includeIncomeConsumption = true; // 소득/소비 포함 여부
        
        @Builder.Default
        private Boolean includeBusinessPopulation = true; // 업소수/인구 포함 여부
        
        @Builder.Default
        private Boolean applyMultiplier = true; // 매출 배수 적용 여부
        
        @Builder.Default
        private Integer retryCount = 3; // 재시도 횟수
        
        @Builder.Default
        private Long delayMs = 100L; // API 호출 간 지연 시간 (ms)
    }

    /**
     * 데이터 조회 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DataSearchReq {
        
        private String adminDongName; // 행정동명 검색
        
        private Long minSales; // 최소 매출
        private Long maxSales; // 최대 매출
        
        private Integer minPopulation; // 최소 인구수
        private Integer maxPopulation; // 최대 인구수
        
        private String sortBy; // 정렬 기준 (SALES, POPULATION, RESTAURANT_COUNT, UPDATED_AT)
        
        @Builder.Default
        private String sortOrder = "DESC"; // 정렬 순서 (ASC, DESC)
        
        @Builder.Default
        private Integer page = 0; // 페이지 번호
        
        @Builder.Default
        private Integer size = 20; // 페이지 크기
    }

    /**
     * 특정 행정동 데이터 조회 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminDongDataReq {
        
        @NotNull(message = "행정동코드는 필수입니다.")
        private Integer adminDongCode;
    }

    /**
     * 다중 행정동 데이터 조회 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MultiAdminDongDataReq {
        
        @NotEmpty(message = "행정동코드 목록은 필수입니다.")
        private List<Integer> adminDongCodes;
    }
}
