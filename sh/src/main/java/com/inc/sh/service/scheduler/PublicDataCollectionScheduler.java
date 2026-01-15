package com.inc.sh.service.scheduler;

import com.inc.sh.config.CityBoxCoordinates;
import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.service.analysis.SalesDataCollectionService;
import com.inc.sh.service.analysis.IncomeConsumptionDataCollectionService;
import com.inc.sh.service.analysis.BusinessPopulationDataCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicDataCollectionScheduler {

    private final SalesDataCollectionService salesDataCollectionService;
    private final IncomeConsumptionDataCollectionService incomeConsumptionDataCollectionService;
    private final BusinessPopulationDataCollectionService businessPopulationDataCollectionService;

    // 7개 광역시 목록
    private final List<String> TARGET_CITIES = Arrays.asList(
            "서울특별시", "부산광역시", "대구광역시", "인천광역시", 
            "광주광역시", "대전광역시", "울산광역시"
    );

//    /**
//     * 테스트용 - 매시간 실행
//     */
//    @Scheduled(cron = "0 0 * * * *") // 매시간 0분에 실행
//    public void collectPublicDataHourly() {
//        log.info("=== 공공데이터 수집 스케줄러 시작 (테스트용 - 매시간) ===");
//        executeDataCollection("테스트");
//    }

    /**
     * 운영용 - 매월 1일 새벽 3시 실행
     */
    @Scheduled(cron = "0 0 3 1 * *") // 매월 1일 새벽 3시
    public void collectPublicDataMonthly() {
        log.info("=== 공공데이터 수집 스케줄러 시작 (운영용 - 매월) ===");
        executeDataCollection("운영");
    }
    
    /**
     * 수동 실행용 메서드 (테스트)
     */
    public void executeDataCollectionManual() {
        log.info("=== 공공데이터 수집 스케줄러 시작 (수동 실행) ===");
        executeDataCollection("수동");
    }

    /**
     * 공공데이터 수집 실행 (순차 처리)
     */
    private void executeDataCollection(String mode) {
        String startTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        log.info("🎯 공공데이터 수집 시작 - 모드: {}, 시작시간: {}", mode, startTime);

        try {
            // 1단계: 매출 데이터 수집
            log.info("📊 1단계: 매출 데이터 수집 시작");
            boolean salesSuccess = collectSalesData();
            if (!salesSuccess) {
                log.error("❌ 매출 데이터 수집 실패 - 스케줄러 중단");
                return;
            }
            log.info("✅ 1단계 완료: 매출 데이터 수집 성공");

            // 2단계: 소득/소비 데이터 수집
            log.info("💰 2단계: 소득/소비 데이터 수집 시작");
            boolean incomeSuccess = collectIncomeConsumptionData();
            if (!incomeSuccess) {
                log.error("❌ 소득/소비 데이터 수집 실패 - 스케줄러 중단");
                return;
            }
            log.info("✅ 2단계 완료: 소득/소비 데이터 수집 성공");

            // 3단계: 업소수/인구 데이터 수집
            log.info("🏪 3단계: 업소수/인구 데이터 수집 시작");
            boolean businessSuccess = collectBusinessPopulationData();
            if (!businessSuccess) {
                log.error("❌ 업소수/인구 데이터 수집 실패");
                return;
            }
            log.info("✅ 3단계 완료: 업소수/인구 데이터 수집 성공");

            String endTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            log.info("🎉 공공데이터 수집 스케줄러 완료 - 모드: {}, 종료시간: {}", mode, endTime);

        } catch (Exception e) {
            log.error("❌ 공공데이터 수집 스케줄러 실행 중 오류 발생", e);
        }
    }

    /**
     * 1단계: 매출 데이터 수집
     */
    private boolean collectSalesData() {
        try {
            PublicDataAnalysisReqDto.SalesDataCollectReq request = 
                PublicDataAnalysisReqDto.SalesDataCollectReq.builder()
                        .cities(TARGET_CITIES)
                        .applyMultiplier(true)
                        .retryCount(3)
                        .delayMs(100L)
                        .build();

            PublicDataAnalysisRespDto.CollectionProgressResp result = 
                    salesDataCollectionService.collectSalesData(request);

            log.info("📊 매출 데이터 수집 결과 - 성공: {}, 실패: {}, 상태: {}", 
                    result.getSuccessRecords(), result.getFailureRecords(), result.getStatus());

            return "COMPLETED".equals(result.getStatus()) && 
                   result.getSuccessRecords() != null && result.getSuccessRecords() > 0;

        } catch (Exception e) {
            log.error("매출 데이터 수집 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 2단계: 소득/소비 데이터 수집
     */
    private boolean collectIncomeConsumptionData() {
        try {
            PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq request = 
                PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq.builder()
                        .cities(TARGET_CITIES)
                        .retryCount(3)
                        .delayMs(100L)
                        .build();

            PublicDataAnalysisRespDto.CollectionProgressResp result = 
                    incomeConsumptionDataCollectionService.collectIncomeConsumptionData(request);

            log.info("💰 소득/소비 데이터 수집 결과 - 성공: {}, 실패: {}, 상태: {}", 
                    result.getSuccessRecords(), result.getFailureRecords(), result.getStatus());

            return "COMPLETED".equals(result.getStatus()) && 
                   result.getSuccessRecords() != null && result.getSuccessRecords() > 0;

        } catch (Exception e) {
            log.error("소득/소비 데이터 수집 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 3단계: 업소수/인구 데이터 수집
     */
    private boolean collectBusinessPopulationData() {
        try {
            // CityBoxCoordinates에서 지역 코드 가져오기
            List<String> areaCodes = Arrays.asList(CityBoxCoordinates.AREA_CODES);

            PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq request = 
                PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq.builder()
                        .areaCodes(areaCodes)
                        .includeBusinessCount(true)
                        .includePopulationData(true)
                        .retryCount(3)
                        .delayMs(100L)
                        .build();

            PublicDataAnalysisRespDto.CollectionProgressResp result = 
                    businessPopulationDataCollectionService.collectBusinessPopulationData(request);

            log.info("🏪 업소수/인구 데이터 수집 결과 - 성공: {}, 실패: {}, 상태: {}", 
                    result.getSuccessRecords(), result.getFailureRecords(), result.getStatus());

            return "COMPLETED".equals(result.getStatus());

        } catch (Exception e) {
            log.error("업소수/인구 데이터 수집 중 오류 발생", e);
            return false;
        }
    }
}