package com.inc.sh.dto.publicDataAnalysis.respDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.inc.sh.entity.PublicDataAnalysis;

/**
 * 공공데이터 분석 응답 DTO 클래스들
 */
public class PublicDataAnalysisRespDto {

    /**
     * 분석 데이터 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AnalysisDataResp {
        private Long analysisCode;
        private Integer adminDongCode;
        private String adminDongName;

        // 매출 정보
        private RestaurantData koreanFood;
        private RestaurantData chineseFood;
        private RestaurantData japaneseFood;
        private RestaurantData westernFood;
        private RestaurantData southeastAsianFood;

        // 인구 정보
        private PopulationData populationData;

        // 경제 정보
        private EconomicData economicData;

        // 통계 정보
        private StatisticsData statistics;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class RestaurantData {
            private Integer restaurantCount;
            private Long sales;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class PopulationData {
            private Integer floatingPopulation;
            private Integer residentialPopulation;
            private Integer workingPopulation;
            private Integer householdCount;
            private Integer totalPopulation;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class EconomicData {
            private Long income;
            private Long consumption;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class StatisticsData {
            private Long totalSales;
            private Integer totalRestaurantCount;
            private Integer totalPopulation;
        }
    }

    /**
     * 데이터 수집 진행 상황 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionProgressResp {
        private String taskId; // 작업 ID
        private String status; // PROCESSING, COMPLETED, FAILED
        private String currentStep; // SALES, INCOME_CONSUMPTION, BUSINESS_POPULATION
        private String currentCity; // 현재 처리 중인 광역시
        private String currentAreaCode; // 현재 처리 중인 지역코드
        
        private Integer totalSteps; // 전체 단계 수
        private Integer completedSteps; // 완료된 단계 수
        
        private Integer totalApiCalls; // 전체 API 호출 수
        private Integer completedApiCalls; // 완료된 API 호출 수
        private Integer successApiCalls; // 성공한 API 호출 수
        private Integer failureApiCalls; // 실패한 API 호출 수
        
        private Integer totalRecords; // 전체 레코드 수
        private Integer processedRecords; // 처리된 레코드 수
        private Integer successRecords; // 성공한 레코드 수
        private Integer failureRecords; // 실패한 레코드 수
        
        private LocalDateTime startTime; // 시작 시간
        private LocalDateTime endTime; // 종료 시간
        private Long elapsedTimeMs; // 소요 시간 (밀리초)
        
        private Double progressPercent; // 진행률 (%)
        private String estimatedRemainingTime; // 예상 남은 시간
        
        private List<String> errorMessages; // 오류 메시지 목록
        private List<String> warnings; // 경고 메시지 목록
    }

    /**
     * API 공통 응답 래퍼 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApiResponse<T> {
        private Boolean success;
        private String message;
        private T data;
        private String errorCode;
        private LocalDateTime timestamp;

        public static <T> ApiResponse<T> success(T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message("SUCCESS")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> failure(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .build();
        }

        public static <T> ApiResponse<T> failure(String message, String errorCode) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .errorCode(errorCode)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 페이징 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PagedResponse<T> {
        private List<T> content;
        private Integer totalPages;
        private Long totalElements;
        private Integer currentPage;
        private Integer pageSize;
        private Boolean hasNext;
        private Boolean hasPrevious;
        private Boolean first;
        private Boolean last;
    }

    /**
     * 통계 요약 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatisticsSummaryResp {
        private Long totalDataCount; // 전체 데이터 개수
        private Long totalSalesSum; // 전체 매출 합계
        private Long totalRestaurantCountSum; // 전체 업소수 합계
        private Long totalPopulationSum; // 전체 인구수 합계
        
        private Long dataWithSalesCount; // 매출 데이터가 있는 개수
        private Long dataWithPopulationCount; // 인구 데이터가 있는 개수
        private Long incompleteDataCount; // 불완전한 데이터 개수
        
        private LocalDateTime lastUpdated; // 마지막 업데이트 시간
        
        private List<TopRankingData> topSalesRanking; // 매출 상위 랭킹
        private List<TopRankingData> topPopulationRanking; // 인구 상위 랭킹
        private List<TopRankingData> topRestaurantRanking; // 업소수 상위 랭킹
        
        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class TopRankingData {
            private Integer rank;
            private Integer adminDongCode;
            private String adminDongName;
            private Long value;
        }
    }

    /**
     * 공공 API 원본 응답 DTO (매출)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SalesApiResp {
        private String storeCnt;
        private String saleAmt;
        private String admCd;
        private String admNm;
        private Double yAxis;
        private Double xAxis;
    }

    /**
     * 공공 API 원본 응답 DTO (소비)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsumptionApiResp {
        private String wholCnsmpAmt;
        private String admCd;
        private String admNm;
        private Integer ro;
        private Double yAxis;
        private Double xAxis;
    }

    /**
     * 공공 API 원본 응답 DTO (소득)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IncomeApiResp {
        private String wholEarnAmt;
        private String admCd;
        private String admNm;
        private Integer ro;
        private Double yAxis;
        private Double xAxis;
    }

    /**
     * 공공 API 원본 응답 DTO (업소수/유동인구)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DynpplSttusApiResp {
        private String areaNm;
        private String areaGb;
        private String areaCd;
        private String upsoCnt;
        private String dynPopnum;
        private String dynMalePopnum;
        private String dynFemalePopnum;
    }

    /**
     * 공공 API 원본 응답 DTO (세대수/주거인구)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RgnSttusApiResp {
        private String areaNm;
        private String areaGb;
        private String areaCd;
        private String hous;
        private String pop;
        private String upsoCnt;
    }

    /**
     * 공공 API 원본 응답 DTO (직장인구)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WrcpplSttusApiResp {
        private String areaNm;
        private String areaCd;
        private String areaGb;
        private String wrcPopnum;
        private String upsoCnt;
    }
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor 
    @AllArgsConstructor
    public static class PublicDataAnalysisDetailResp {
        private Integer adminDongCode;
        private String adminDongName;
        
        // 매출 데이터
        private Long koreanSales;
        private Long chineseSales;
        private Long japaneseSales;
        private Long westernSales;
        private Long southeastAsianSales;
        
        // 업소수 데이터
        private Integer koreanRestaurantCount;
        private Integer chineseRestaurantCount;
        private Integer japaneseRestaurantCount;
        private Integer westernRestaurantCount;
        private Integer southeastAsianRestaurantCount;
        
        // 인구 데이터
        private Integer floatingPopulation;
        private Integer residentialPopulation;
        private Integer workingPopulation;
        private Integer householdCount;
        
        // 소득/소비 데이터 (향후 수집될 데이터)
        private Long income;
        private Long consumption;
        
        // 메타 정보
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Entity → DTO 변환
        public static PublicDataAnalysisDetailResp fromEntity(PublicDataAnalysis entity) {
            return PublicDataAnalysisDetailResp.builder()
                    .adminDongCode(entity.getAdminDongCode())
                    .adminDongName(entity.getAdminDongName())
                    .koreanSales(entity.getKoreanSales())
                    .chineseSales(entity.getChineseSales())
                    .japaneseSales(entity.getJapaneseSales())
                    .westernSales(entity.getWesternSales())
                    .southeastAsianSales(entity.getSoutheastAsianSales())
                    .koreanRestaurantCount(entity.getKoreanRestaurantCount())
                    .chineseRestaurantCount(entity.getChineseRestaurantCount())
                    .japaneseRestaurantCount(entity.getJapaneseRestaurantCount())
                    .westernRestaurantCount(entity.getWesternRestaurantCount())
                    .southeastAsianRestaurantCount(entity.getSoutheastAsianRestaurantCount())
                    .floatingPopulation(entity.getFloatingPopulation())
                    .residentialPopulation(entity.getResidentialPopulation())
                    .workingPopulation(entity.getWorkingPopulation())
                    .householdCount(entity.getHouseholdCount())
                    .income(entity.getIncome())
                    .consumption(entity.getConsumption())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }
    }
}
