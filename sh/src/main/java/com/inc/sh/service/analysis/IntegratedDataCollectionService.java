package com.inc.sh.service.analysis;

import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 통합 공공데이터 수집 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IntegratedDataCollectionService {

    private final SalesDataCollectionService salesDataCollectionService;
    private final IncomeConsumptionDataCollectionService incomeConsumptionDataCollectionService;
    private final BusinessPopulationDataCollectionService businessPopulationDataCollectionService;

    /**
     * 통합 데이터 수집 메인 메서드
     * 1. 매출 데이터 수집
     * 2. 소득/소비 데이터 수집
     * 3. 업소수/인구 데이터 수집
     */
    public PublicDataAnalysisRespDto.CollectionProgressResp collectIntegratedData(
            PublicDataAnalysisReqDto.IntegratedDataCollectReq request) {

        log.info("통합 공공데이터 수집 시작 - 광역시: {}, 지역코드: {}개", 
                request.getCities(), request.getAreaCodes().size());

        String taskId = "integrated_" + System.currentTimeMillis();
        
        PublicDataAnalysisRespDto.CollectionProgressResp totalProgress = PublicDataAnalysisRespDto.CollectionProgressResp.builder()
                .taskId(taskId)
                .status("PROCESSING")
                .currentStep("INITIALIZATION")
                .totalSteps(calculateTotalSteps(request))
                .completedSteps(0)
                .totalApiCalls(0)
                .completedApiCalls(0)
                .successApiCalls(0)
                .failureApiCalls(0)
                .totalRecords(0)
                .processedRecords(0)
                .successRecords(0)
                .failureRecords(0)
                .startTime(LocalDateTime.now())
                .progressPercent(0.0)
                .errorMessages(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();

        try {
            // 1단계: 매출 데이터 수집
            if (request.getIncludeSales()) {
                log.info("통합 수집 1단계: 매출 데이터 수집 시작");
                totalProgress.setCurrentStep("SALES");
                
                PublicDataAnalysisReqDto.SalesDataCollectReq salesRequest = PublicDataAnalysisReqDto.SalesDataCollectReq.builder()
                        .cities(request.getCities())
                        .applyMultiplier(request.getApplyMultiplier())
                        .retryCount(request.getRetryCount())
                        .delayMs(request.getDelayMs())
                        .build();
                
                PublicDataAnalysisRespDto.CollectionProgressResp salesProgress = 
                        salesDataCollectionService.collectSalesData(salesRequest);
                
                // 진행 상황 통합
                mergeProgress(totalProgress, salesProgress);
                totalProgress.setCompletedSteps(totalProgress.getCompletedSteps() + 1);
                
                if ("FAILED".equals(salesProgress.getStatus())) {
                    totalProgress.setStatus("FAILED");
                    totalProgress.getErrorMessages().add("매출 데이터 수집 실패");
                    log.error("매출 데이터 수집 실패로 인한 통합 수집 중단");
                    return totalProgress;
                }
                
                log.info("통합 수집 1단계: 매출 데이터 수집 완료");
            }

            // 2단계: 소득/소비 데이터 수집
            if (request.getIncludeIncomeConsumption()) {
                log.info("통합 수집 2단계: 소득/소비 데이터 수집 시작");
                totalProgress.setCurrentStep("INCOME_CONSUMPTION");
                
                PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq incomeRequest = 
                        PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq.builder()
                                .cities(request.getCities())
                                .retryCount(request.getRetryCount())
                                .delayMs(request.getDelayMs())
                                .build();
                
                PublicDataAnalysisRespDto.CollectionProgressResp incomeProgress = 
                        incomeConsumptionDataCollectionService.collectIncomeConsumptionData(incomeRequest);
                
                // 진행 상황 통합
                mergeProgress(totalProgress, incomeProgress);
                totalProgress.setCompletedSteps(totalProgress.getCompletedSteps() + 1);
                
                if ("FAILED".equals(incomeProgress.getStatus())) {
                    totalProgress.setStatus("PARTIAL_SUCCESS");
                    totalProgress.getErrorMessages().add("소득/소비 데이터 수집 실패 (부분 성공)");
                    log.warn("소득/소비 데이터 수집 실패, 다음 단계 진행");
                } else {
                    log.info("통합 수집 2단계: 소득/소비 데이터 수집 완료");
                }
            }

            // 3단계: 업소수/인구 데이터 수집
            if (request.getIncludeBusinessPopulation()) {
                log.info("통합 수집 3단계: 업소수/인구 데이터 수집 시작");
                totalProgress.setCurrentStep("BUSINESS_POPULATION");
                
                PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq businessRequest = 
                        PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq.builder()
                                .areaCodes(request.getAreaCodes())
                                .includeBusinessCount(true)
                                .includePopulationData(true)
                                .retryCount(request.getRetryCount())
                                .delayMs(request.getDelayMs())
                                .build();
                
                PublicDataAnalysisRespDto.CollectionProgressResp businessProgress = 
                        businessPopulationDataCollectionService.collectBusinessPopulationData(businessRequest);
                
                // 진행 상황 통합
                mergeProgress(totalProgress, businessProgress);
                totalProgress.setCompletedSteps(totalProgress.getCompletedSteps() + 1);
                
                if ("FAILED".equals(businessProgress.getStatus())) {
                    if (!"FAILED".equals(totalProgress.getStatus())) {
                        totalProgress.setStatus("PARTIAL_SUCCESS");
                    }
                    totalProgress.getErrorMessages().add("업소수/인구 데이터 수집 실패 (부분 성공)");
                    log.warn("업소수/인구 데이터 수집 실패");
                } else {
                    log.info("통합 수집 3단계: 업소수/인구 데이터 수집 완료");
                }
            }

            // 최종 상태 설정
            if (!"FAILED".equals(totalProgress.getStatus()) && !"PARTIAL_SUCCESS".equals(totalProgress.getStatus())) {
                totalProgress.setStatus("COMPLETED");
            }
            
            totalProgress.setCurrentStep("COMPLETED");
            totalProgress.setEndTime(LocalDateTime.now());
            totalProgress.setProgressPercent(100.0);
            
            if (totalProgress.getStartTime() != null) {
                totalProgress.setElapsedTimeMs(
                    java.time.Duration.between(totalProgress.getStartTime(), totalProgress.getEndTime()).toMillis()
                );
            }
            
            log.info("통합 공공데이터 수집 완료 - 상태: {}, API호출: {}/{}, 레코드: {}/{}", 
                    totalProgress.getStatus(),
                    totalProgress.getSuccessApiCalls(), totalProgress.getTotalApiCalls(),
                    totalProgress.getSuccessRecords(), totalProgress.getTotalRecords());

        } catch (Exception e) {
            log.error("통합 공공데이터 수집 중 오류 발생", e);
            totalProgress.setStatus("FAILED");
            totalProgress.setEndTime(LocalDateTime.now());
            totalProgress.getErrorMessages().add("통합 수집 프로세스 오류: " + e.getMessage());
        }

        return totalProgress;
    }

    /**
     * 전체 단계 수 계산
     */
    private int calculateTotalSteps(PublicDataAnalysisReqDto.IntegratedDataCollectReq request) {
        int steps = 0;
        
        if (request.getIncludeSales()) steps++;
        if (request.getIncludeIncomeConsumption()) steps++;
        if (request.getIncludeBusinessPopulation()) steps++;
        
        return Math.max(steps, 1);
    }

    /**
     * 개별 진행 상황을 전체 진행 상황에 통합
     */
    private void mergeProgress(PublicDataAnalysisRespDto.CollectionProgressResp totalProgress,
                             PublicDataAnalysisRespDto.CollectionProgressResp stepProgress) {
        
        // API 호출 통계 누적
        totalProgress.setTotalApiCalls(totalProgress.getTotalApiCalls() + stepProgress.getTotalApiCalls());
        totalProgress.setCompletedApiCalls(totalProgress.getCompletedApiCalls() + stepProgress.getCompletedApiCalls());
        totalProgress.setSuccessApiCalls(totalProgress.getSuccessApiCalls() + stepProgress.getSuccessApiCalls());
        totalProgress.setFailureApiCalls(totalProgress.getFailureApiCalls() + stepProgress.getFailureApiCalls());
        
        // 레코드 통계 누적
        totalProgress.setTotalRecords(totalProgress.getTotalRecords() + stepProgress.getTotalRecords());
        totalProgress.setProcessedRecords(totalProgress.getProcessedRecords() + stepProgress.getProcessedRecords());
        totalProgress.setSuccessRecords(totalProgress.getSuccessRecords() + stepProgress.getSuccessRecords());
        totalProgress.setFailureRecords(totalProgress.getFailureRecords() + stepProgress.getFailureRecords());
        
        // 에러 메시지 통합
        if (stepProgress.getErrorMessages() != null && !stepProgress.getErrorMessages().isEmpty()) {
            totalProgress.getErrorMessages().addAll(stepProgress.getErrorMessages());
        }
        
        // 경고 메시지 통합
        if (stepProgress.getWarnings() != null && !stepProgress.getWarnings().isEmpty()) {
            totalProgress.getWarnings().addAll(stepProgress.getWarnings());
        }
        
        // 진행률 계산 (단계별)
        double stepProgressPercent = (double) totalProgress.getCompletedSteps() / totalProgress.getTotalSteps() * 100;
        totalProgress.setProgressPercent(stepProgressPercent);
    }

    /**
     * 예상 소요 시간 계산
     */
    private String calculateEstimatedRemainingTime(PublicDataAnalysisRespDto.CollectionProgressResp progress) {
        if (progress.getElapsedTimeMs() == null || progress.getElapsedTimeMs() <= 0) {
            return "계산 중...";
        }
        
        if (progress.getProgressPercent() <= 0) {
            return "알 수 없음";
        }
        
        double progressRatio = progress.getProgressPercent() / 100.0;
        long totalEstimatedTime = (long) (progress.getElapsedTimeMs() / progressRatio);
        long remainingTime = totalEstimatedTime - progress.getElapsedTimeMs();
        
        if (remainingTime <= 0) {
            return "곧 완료";
        }
        
        long remainingMinutes = remainingTime / (1000 * 60);
        long remainingSeconds = (remainingTime % (1000 * 60)) / 1000;
        
        if (remainingMinutes > 0) {
            return String.format("%d분 %d초", remainingMinutes, remainingSeconds);
        } else {
            return String.format("%d초", remainingSeconds);
        }
    }

    /**
     * 기본 전체 광역시 및 지역코드로 통합 수집 실행
     */
    public PublicDataAnalysisRespDto.CollectionProgressResp collectAllData() {
        
        PublicDataAnalysisReqDto.IntegratedDataCollectReq request = PublicDataAnalysisReqDto.IntegratedDataCollectReq.builder()
                .cities(Arrays.asList("서울특별시", "부산광역시", "대구광역시", "인천광역시", 
                                    "광주광역시", "대전광역시", "울산광역시"))
                .areaCodes(Arrays.asList(
                    // 서울특별시 (25개)
                    "11010", "11020", "11030", "11040", "11050", "11060", "11070", "11080", "11090", "11100",
                    "11110", "11120", "11130", "11140", "11150", "11160", "11170", "11180", "11190", "11200",
                    "11210", "11220", "11230", "11240", "11250",
                    // 부산광역시 (16개)
                    "26110", "26120", "26130", "26140", "26150", "26160", "26170", "26180", "26350", "26380",
                    "26410", "26440", "26470", "26500", "26530", "26710",
                    // 대구광역시 (8개)
                    "27110", "27140", "27170", "27200", "27230", "27260", "27290", "27710",
                    // 인천광역시 (10개)
                    "28110", "28140", "28177", "28185", "28200", "28237", "28245", "28260", "28710", "28720",
                    // 광주광역시 (5개)  
                    "29110", "29140", "29155", "29170", "29200",
                    // 대전광역시 (5개)
                    "30110", "30140", "30170", "30200", "30230",
                    // 울산광역시 (5개)
                    "31110", "31140", "31170", "31200", "31710",
                    // 세종특별자치시 (1개)
                    "36110"
                ))
                .includeSales(true)
                .includeIncomeConsumption(true)
                .includeBusinessPopulation(true)
                .applyMultiplier(true)
                .retryCount(3)
                .delayMs(100L)
                .build();
        
        return collectIntegratedData(request);
    }
}