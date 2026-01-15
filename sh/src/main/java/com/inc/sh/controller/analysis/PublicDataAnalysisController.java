package com.inc.sh.controller.analysis;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.service.analysis.SalesDataCollectionService;

import jakarta.validation.Valid;

import com.inc.sh.service.analysis.IncomeConsumptionDataCollectionService;
import com.inc.sh.service.analysis.BusinessPopulationDataCollectionService;
import com.inc.sh.service.analysis.IntegratedDataCollectionService;
import com.inc.sh.service.analysis.PublicDataAnalysisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

/**
 * 공공데이터 분석 컨트롤러
 */
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PublicDataAnalysisController {

    private final SalesDataCollectionService salesDataCollectionService;
    private final IncomeConsumptionDataCollectionService incomeConsumptionDataCollectionService;
    private final BusinessPopulationDataCollectionService businessPopulationDataCollectionService;
    private final IntegratedDataCollectionService integratedDataCollectionService;
    private final PublicDataAnalysisService publicDataAnalysisService;

    /**
     * 1. 매출 데이터 수집 API
     * POST /api/v1/analysis/collect/sales
     */
    @PostMapping("/collect/sales")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectSalesData(
            @Valid @RequestBody PublicDataAnalysisReqDto.SalesDataCollectReq request) {
        
        log.info("매출 데이터 수집 요청 - 광역시: {}", request.getCities());
        
        try {
            PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                    salesDataCollectionService.collectSalesData(request);
            
            String message = String.format("매출 데이터 수집 %s - API호출: %d/%d, 레코드: %d/%d",
                    "COMPLETED".equals(progress.getStatus()) ? "완료" : 
                    "FAILED".equals(progress.getStatus()) ? "실패" : "진행중",
                    progress.getSuccessApiCalls(), progress.getTotalApiCalls(),
                    progress.getSuccessRecords(), progress.getTotalRecords());
            
            return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(progress, message));
            
        } catch (Exception e) {
            log.error("매출 데이터 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(PublicDataAnalysisRespDto.ApiResponse.failure(
                            "매출 데이터 수집 실패: " + e.getMessage(), "SALES_COLLECTION_ERROR"));
        }
    }

    /**
     * 2. 소득/소비 데이터 수집 API
     * POST /api/v1/analysis/collect/income-consumption
     */
    @PostMapping("/collect/income-consumption")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectIncomeConsumptionData(
            @Valid @RequestBody PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq request) {
        
        log.info("소득/소비 데이터 수집 요청 - 광역시: {}", request.getCities());
        
        try {
            PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                    incomeConsumptionDataCollectionService.collectIncomeConsumptionData(request);
            
            String message = String.format("소득/소비 데이터 수집 %s - API호출: %d/%d, 레코드: %d/%d",
                    "COMPLETED".equals(progress.getStatus()) ? "완료" : 
                    "FAILED".equals(progress.getStatus()) ? "실패" : "진행중",
                    progress.getSuccessApiCalls(), progress.getTotalApiCalls(),
                    progress.getSuccessRecords(), progress.getTotalRecords());
            
            return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(progress, message));
            
        } catch (Exception e) {
            log.error("소득/소비 데이터 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(PublicDataAnalysisRespDto.ApiResponse.failure(
                            "소득/소비 데이터 수집 실패: " + e.getMessage(), "INCOME_CONSUMPTION_COLLECTION_ERROR"));
        }
    }

    /**
     * 3. 업소수/인구 데이터 수집 API
     * POST /api/v1/analysis/collect/business-population
     */
    @PostMapping("/collect/business-population")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectBusinessPopulationData(
            @Valid @RequestBody PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq request) {
        
        log.info("업소수/인구 데이터 수집 요청 - 지역코드: {}개", request.getAreaCodes().size());
        
        try {
            PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                    businessPopulationDataCollectionService.collectBusinessPopulationData(request);
            
            String message = String.format("업소수/인구 데이터 수집 %s - API호출: %d/%d, 레코드: %d/%d",
                    "COMPLETED".equals(progress.getStatus()) ? "완료" : 
                    "FAILED".equals(progress.getStatus()) ? "실패" : "진행중",
                    progress.getSuccessApiCalls(), progress.getTotalApiCalls(),
                    progress.getSuccessRecords(), progress.getTotalRecords());
            
            return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(progress, message));
            
        } catch (Exception e) {
            log.error("업소수/인구 데이터 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(PublicDataAnalysisRespDto.ApiResponse.failure(
                            "업소수/인구 데이터 수집 실패: " + e.getMessage(), "BUSINESS_POPULATION_COLLECTION_ERROR"));
        }
    }

    /**
     * 4. 통합 데이터 수집 API (1→2→3 순차 실행)
     * POST /api/v1/analysis/collect/integrated
     */
    @PostMapping("/collect/integrated")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectIntegratedData(
            @Valid @RequestBody PublicDataAnalysisReqDto.IntegratedDataCollectReq request) {
        
        log.info("통합 데이터 수집 요청 - 광역시: {}, 지역코드: {}개", 
                request.getCities(), request.getAreaCodes().size());
        
        try {
            PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                    integratedDataCollectionService.collectIntegratedData(request);
            
            String message = String.format("통합 데이터 수집 %s - API호출: %d/%d, 레코드: %d/%d",
                    "COMPLETED".equals(progress.getStatus()) ? "완료" : 
                    "PARTIAL_SUCCESS".equals(progress.getStatus()) ? "부분 성공" :
                    "FAILED".equals(progress.getStatus()) ? "실패" : "진행중",
                    progress.getSuccessApiCalls(), progress.getTotalApiCalls(),
                    progress.getSuccessRecords(), progress.getTotalRecords());
            
            return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(progress, message));
            
        } catch (Exception e) {
            log.error("통합 데이터 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(PublicDataAnalysisRespDto.ApiResponse.failure(
                            "통합 데이터 수집 실패: " + e.getMessage(), "INTEGRATED_COLLECTION_ERROR"));
        }
    }

    /**
     * 5. 전체 데이터 수집 API (모든 광역시 + 모든 지역코드)
     * POST /api/v1/analysis/collect/all
     */
    @PostMapping("/collect/all")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectAllData() {
        
        log.info("전체 데이터 수집 요청 (모든 광역시 + 모든 지역코드)");
        
        try {
            PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                    integratedDataCollectionService.collectAllData();
            
            String message = String.format("전체 데이터 수집 %s - API호출: %d/%d, 레코드: %d/%d",
                    "COMPLETED".equals(progress.getStatus()) ? "완료" : 
                    "PARTIAL_SUCCESS".equals(progress.getStatus()) ? "부분 성공" :
                    "FAILED".equals(progress.getStatus()) ? "실패" : "진행중",
                    progress.getSuccessApiCalls(), progress.getTotalApiCalls(),
                    progress.getSuccessRecords(), progress.getTotalRecords());
            
            return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(progress, message));
            
        } catch (Exception e) {
            log.error("전체 데이터 수집 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(PublicDataAnalysisRespDto.ApiResponse.failure(
                            "전체 데이터 수집 실패: " + e.getMessage(), "ALL_COLLECTION_ERROR"));
        }
    }

    /**
     * 간단한 요청 생성 유틸리티 API들
     */

    /**
     * 매출 데이터 수집 (기본 설정)
     * POST /api/v1/analysis/collect/sales/simple
     */
    @PostMapping("/collect/sales/simple")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectSalesDataSimple(
            @RequestParam(value = "cities", required = false, defaultValue = "서울특별시,부산광역시,대구광역시,인천광역시,광주광역시,대전광역시,울산광역시") String cities) {
        
        PublicDataAnalysisReqDto.SalesDataCollectReq request = PublicDataAnalysisReqDto.SalesDataCollectReq.builder()
                .cities(Arrays.asList(cities.split(",")))
                .applyMultiplier(true)
                .retryCount(3)
                .delayMs(100L)
                .build();
        
        return collectSalesData(request);
    }

    /**
     * 소득/소비 데이터 수집 (기본 설정)
     * POST /api/v1/analysis/collect/income-consumption/simple
     */
    @PostMapping("/collect/income-consumption/simple")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectIncomeConsumptionDataSimple(
            @RequestParam(value = "cities", required = false, defaultValue = "서울특별시,부산광역시,대구광역시,인천광역시,광주광역시,대전광역시,울산광역시") String cities) {
        
        PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq request = PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq.builder()
                .cities(Arrays.asList(cities.split(",")))
                .retryCount(3)
                .delayMs(100L)
                .build();
        
        return collectIncomeConsumptionData(request);
    }

    /**
     * 업소수/인구 데이터 수집 (기본 설정)
     * POST /api/v1/analysis/collect/business-population/simple
     */
    @PostMapping("/collect/business-population/simple")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> collectBusinessPopulationDataSimple() {
        
        // 모든 지역코드 사용
        PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq request = PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq.builder()
                .areaCodes(Arrays.asList(
                    // 서울특별시 (25개)
                		"1111","1114","1117","1120","1121","1123","1126","1129","1130","1132",
                		"1135","1138","1141","1144","1147","1150","1153","1154","1156","1159",
                		"1162","1165","1168","1171","1174",
                    // 부산광역시 (16개)
                	  "2611", "2614", "2617", "2620", "2623", "2626", "2629", "2632", "2635", "2638", "2641", "2644", "2647", "2650", "2653", "2671",
                    // 대구광역시 (8개)
                      "2711", "2714", "2717", "2720", "2723", "2726", "2729", "2771", "2772",
                    // 인천광역시 (10개)
                      "2811", "2814", "2817", "2818", "2820", "2823", "2824", "2826", "2871", "2872",
                    // 광주광역시 (5개)  
                      "2911","2914","2915","2917","2920",
                    // 대전광역시 (5개)
                      "3011", "3014", "3017", "3020", "3023",
                    // 울산광역시 (5개)
                      "3111", "3114", "3117", "3120", "3171"
                ))
                .includeBusinessCount(true)
                .includePopulationData(true)
                .retryCount(3)
                .delayMs(100L)
                .build();
        
        return collectBusinessPopulationData(request);
    }

    /**
     * API 상태 확인
     * GET /api/v1/analysis/health
     */
    @GetMapping("/health")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(
                "공공데이터 분석 API 정상 작동 중", "Health Check OK"));
    }

    /**
     * 설정 정보 조회
     * GET /api/v1/analysis/config
     */
    @GetMapping("/config")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<Object>> getConfig() {
        
        Object config = Map.of(
                "supportedCities", Arrays.asList("서울특별시", "부산광역시", "대구광역시", 
                                               "인천광역시", "광주광역시", "대전광역시", "울산광역시"),
                "totalAreaCodes", 75,
                "totalBoxes", 242,
                "upjongCodes", Map.of(
                        "sales", Arrays.asList("I20101", "I20201", "I20301", "I20402", "I20501"),
                        "business", Arrays.asList("I201", "I202", "I203", "I204", "I205")
                ),
                "salesMultiplier", Map.of(
                        "I20101", 3.0,   // 한식 x3
                        "I20201", 2.0,   // 중식 x2
                        "I20301", 3.5,   // 일식 x3.5
                        "I20402", 3.5,   // 서양식 x3.5
                        "I20501", 1.5    // 동남아식 x1.5
                )
        );
        
        return ResponseEntity.ok(PublicDataAnalysisRespDto.ApiResponse.success(config, "설정 정보 조회 성공"));
    }
    
    
    /**
     * 단일 지역 테스트 API (디버깅용)
     * POST /api/v1/analysis/collect/business-population/test
     */
    @PostMapping("/collect/business-population/test")
    public ResponseEntity<PublicDataAnalysisRespDto.ApiResponse<PublicDataAnalysisRespDto.CollectionProgressResp>> testSingleAreaCollection(
            @RequestParam(value = "areaCode", defaultValue = "11010") String areaCode) {
        
        log.info("단일 지역 테스트 - 지역코드: {}", areaCode);
        
        PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq request = PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq.builder()
                .areaCodes(Arrays.asList(areaCode))
                .includeBusinessCount(true)
                .includePopulationData(true)
                .retryCount(1)
                .delayMs(50L)
                .build();
        
        return collectBusinessPopulationData(request);
    }
    
    /**
     * 동코드로 분석 데이터 상세 조회
     */
    @GetMapping("/{adminDongCode}")
    public ResponseEntity<RespDto<PublicDataAnalysisRespDto.PublicDataAnalysisDetailResp>> getAnalysisData(
            @PathVariable("adminDongCode") Integer adminDongCode) {
        
        log.info("분석 데이터 조회 요청 - 동코드: {}", adminDongCode);
        
        try {
            PublicDataAnalysisRespDto.PublicDataAnalysisDetailResp data = 
                    publicDataAnalysisService.getAnalysisData(adminDongCode);
            
            return ResponseEntity.ok(
                RespDto.success("분석 데이터 조회 성공", data)
            );
            
        } catch (IllegalArgumentException e) {
            log.warn("존재하지 않는 동코드: {}", adminDongCode);
            return ResponseEntity.ok(
                RespDto.fail("존재하지 않는 동코드입니다: " + adminDongCode)
            );
            
        } catch (Exception e) {
            log.error("분석 데이터 조회 실패 - 동코드: {}", adminDongCode, e);
            return ResponseEntity.ok(
                RespDto.fail("분석 데이터 조회 중 오류가 발생했습니다")
            );
        }
    }
}