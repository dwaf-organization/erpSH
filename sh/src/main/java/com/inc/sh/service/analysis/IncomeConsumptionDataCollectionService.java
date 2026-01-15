package com.inc.sh.service.analysis;

import com.inc.sh.config.CityBoxCoordinates;
import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.entity.PublicDataAnalysis;
import com.inc.sh.repository.PublicDataAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 소득/소비 데이터 수집 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class IncomeConsumptionDataCollectionService {

    private final PublicDataAnalysisRepository repository;
    private final RestTemplate restTemplate;

    @Value("${public-api.consumption.base-url:https://bigdata.sbiz.or.kr/gis/api/getMapRadsWholCnsmpAmt.json}")
    private String consumptionApiBaseUrl;

    @Value("${public-api.income.base-url:https://bigdata.sbiz.or.kr/gis/api/getMapRadsWholEarnAmt.json}")
    private String incomeApiBaseUrl;

    @Value("${public-api.common.params.mapLevel:3}")
    private String mapLevel;

    @Value("${public-api.common.params.substr:8}")
    private String substr;

    @Value("${public-api.common.params.bzznType:1}")
    private String bzznType;

    /**
     * 소득/소비 데이터 수집 메인 메서드
     */
    public PublicDataAnalysisRespDto.CollectionProgressResp collectIncomeConsumptionData(
            PublicDataAnalysisReqDto.IncomeConsumptionDataCollectReq request) {

        log.info("소득/소비 데이터 수집 시작 - 대상 광역시: {}", request.getCities());

        String taskId = "income_consumption_" + System.currentTimeMillis();
        
        PublicDataAnalysisRespDto.CollectionProgressResp progress = PublicDataAnalysisRespDto.CollectionProgressResp.builder()
                .taskId(taskId)
                .status("PROCESSING")
                .currentStep("INCOME_CONSUMPTION")
                .totalSteps(1)
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

        // 전체 API 호출 수 계산 (박스 * 2개 API: 소득 + 소비)
        int totalApiCalls = request.getCities().stream()
                .mapToInt(city -> {
                    List<CityBoxCoordinates.BoxCoordinate> boxes = CityBoxCoordinates.getBoxes(city);
                    return boxes.size() * 2; // 소득 + 소비
                })
                .sum();
        
        progress.setTotalApiCalls(totalApiCalls);

        try {
            // 중복 제거를 위한 맵 (행정동코드 -> 소득/소비 데이터)
            Map<Integer, IncomeConsumptionData> adminDongDataMap = new ConcurrentHashMap<>();

            for (String city : request.getCities()) {
                progress.setCurrentCity(city);
                log.info("광역시 {} 소득/소비 데이터 수집 시작", city);

                List<CityBoxCoordinates.BoxCoordinate> boxes = CityBoxCoordinates.getBoxes(city);
                
                for (CityBoxCoordinates.BoxCoordinate box : boxes) {
                    
                    // 소비 데이터 수집
                    try {
                        String consumptionApiUrl = buildConsumptionApiUrl(box);
                        List<PublicDataAnalysisRespDto.ConsumptionApiResp> consumptionResponse = callConsumptionApi(consumptionApiUrl);
                        
                        for (PublicDataAnalysisRespDto.ConsumptionApiResp resp : consumptionResponse) {
                            if (isValidData(resp.getAdmCd(), resp.getAdmNm())) {
                                Integer adminDongCode = parseAdminDongCode(resp.getAdmCd());
                                Long consumption = parseAmount(resp.getWholCnsmpAmt()) * 10000; // 만원 -> 원
                                
                                adminDongDataMap.computeIfAbsent(adminDongCode, k -> 
                                    IncomeConsumptionData.builder()
                                            .adminDongCode(adminDongCode)
                                            .adminDongName(resp.getAdmNm())
                                            .build());
                                
                                adminDongDataMap.get(adminDongCode).setConsumption(consumption);
                            }
                        }
                        
                        progress.setSuccessApiCalls(progress.getSuccessApiCalls() + 1);
                        
                    } catch (Exception e) {
                        log.error("소비 API 호출 실패 - City: {}, Box: {}, Error: {}", 
                                city, box.toString(), e.getMessage(), e);
                        progress.setFailureApiCalls(progress.getFailureApiCalls() + 1);
                        progress.getErrorMessages().add(String.format("소비 API 호출 실패 - %s: %s", 
                                city, e.getMessage()));
                    }
                    
                    progress.setCompletedApiCalls(progress.getCompletedApiCalls() + 1);
                    
                    // 소득 데이터 수집
                    try {
                        String incomeApiUrl = buildIncomeApiUrl(box);
                        List<PublicDataAnalysisRespDto.IncomeApiResp> incomeResponse = callIncomeApi(incomeApiUrl);
                        
                        for (PublicDataAnalysisRespDto.IncomeApiResp resp : incomeResponse) {
                            if (isValidData(resp.getAdmCd(), resp.getAdmNm())) {
                                Integer adminDongCode = parseAdminDongCode(resp.getAdmCd());
                                Long income = parseAmount(resp.getWholEarnAmt()) * 10000; // 만원 -> 원
                                
                                adminDongDataMap.computeIfAbsent(adminDongCode, k -> 
                                    IncomeConsumptionData.builder()
                                            .adminDongCode(adminDongCode)
                                            .adminDongName(resp.getAdmNm())
                                            .build());
                                
                                adminDongDataMap.get(adminDongCode).setIncome(income);
                            }
                        }
                        
                        progress.setSuccessApiCalls(progress.getSuccessApiCalls() + 1);
                        
                    } catch (Exception e) {
                        log.error("소득 API 호출 실패 - City: {}, Box: {}, Error: {}", 
                                city, box.toString(), e.getMessage(), e);
                        progress.setFailureApiCalls(progress.getFailureApiCalls() + 1);
                        progress.getErrorMessages().add(String.format("소득 API 호출 실패 - %s: %s", 
                                city, e.getMessage()));
                    }
                    
                    progress.setCompletedApiCalls(progress.getCompletedApiCalls() + 1);
                    
                    // 지연 시간
                    if (request.getDelayMs() > 0) {
                        Thread.sleep(request.getDelayMs());
                    }
                    
                    // 진행률 업데이트
                    double progressPercent = (double) progress.getCompletedApiCalls() / totalApiCalls * 100;
                    progress.setProgressPercent(progressPercent);
                }
                
                log.info("광역시 {} 소득/소비 데이터 수집 완료", city);
            }

            // DB에 업데이트
            updateIncomeConsumptionData(adminDongDataMap, progress);

            progress.setStatus("COMPLETED");
            progress.setCompletedSteps(1);
            progress.setEndTime(LocalDateTime.now());
            
            if (progress.getStartTime() != null) {
                progress.setElapsedTimeMs(
                    java.time.Duration.between(progress.getStartTime(), progress.getEndTime()).toMillis()
                );
            }
            
            log.info("소득/소비 데이터 수집 완료 - 성공: {}개, 실패: {}개", 
                    progress.getSuccessRecords(), progress.getFailureRecords());

        } catch (Exception e) {
            log.error("소득/소비 데이터 수집 중 오류 발생", e);
            progress.setStatus("FAILED");
            progress.setEndTime(LocalDateTime.now());
            progress.getErrorMessages().add("전체 수집 프로세스 오류: " + e.getMessage());
        }

        return progress;
    }

    /**
     * 소비 API URL 생성
     */
    private String buildConsumptionApiUrl(CityBoxCoordinates.BoxCoordinate box) {
        return String.format("%s?mapLevel=%s&substr=%s&%s&upjongCd=&bzznType=%s",
                consumptionApiBaseUrl, mapLevel, substr, box.toUrlParams(), bzznType);
    }

    /**
     * 소득 API URL 생성
     */
    private String buildIncomeApiUrl(CityBoxCoordinates.BoxCoordinate box) {
        return String.format("%s?mapLevel=%s&substr=%s&%s&upjongCd=&bzznType=%s",
                incomeApiBaseUrl, mapLevel, substr, box.toUrlParams(), bzznType);
    }

    /**
     * 소비 API 호출
     */
    @SuppressWarnings("unchecked")
    private List<PublicDataAnalysisRespDto.ConsumptionApiResp> callConsumptionApi(String apiUrl) throws Exception {
        log.debug("소비 API 호출: {}", apiUrl);
        
        try {
            Object response = restTemplate.getForObject(apiUrl, Object.class);
            
            if (response instanceof List) {
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) response;
                List<PublicDataAnalysisRespDto.ConsumptionApiResp> consumptionList = new ArrayList<>();
                
                for (Map<String, Object> item : responseList) {
                    PublicDataAnalysisRespDto.ConsumptionApiResp consumptionResp = PublicDataAnalysisRespDto.ConsumptionApiResp.builder()
                            .wholCnsmpAmt(String.valueOf(item.get("wholCnsmpAmt")))
                            .admCd(String.valueOf(item.get("admCd")))
                            .admNm(String.valueOf(item.get("admNm")))
                            .ro((Integer) item.get("ro"))
                            .build();
                    
                    consumptionList.add(consumptionResp);
                }
                
                return consumptionList;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("소비 API 호출 실패: {}", apiUrl, e);
            throw e;
        }
    }

    /**
     * 소득 API 호출
     */
    @SuppressWarnings("unchecked")
    private List<PublicDataAnalysisRespDto.IncomeApiResp> callIncomeApi(String apiUrl) throws Exception {
        log.debug("소득 API 호출: {}", apiUrl);
        
        try {
            Object response = restTemplate.getForObject(apiUrl, Object.class);
            
            if (response instanceof List) {
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) response;
                List<PublicDataAnalysisRespDto.IncomeApiResp> incomeList = new ArrayList<>();
                
                for (Map<String, Object> item : responseList) {
                    PublicDataAnalysisRespDto.IncomeApiResp incomeResp = PublicDataAnalysisRespDto.IncomeApiResp.builder()
                            .wholEarnAmt(String.valueOf(item.get("wholEarnAmt")))
                            .admCd(String.valueOf(item.get("admCd")))
                            .admNm(String.valueOf(item.get("admNm")))
                            .ro((Integer) item.get("ro"))
                            .build();
                    
                    incomeList.add(incomeResp);
                }
                
                return incomeList;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("소득 API 호출 실패: {}", apiUrl, e);
            throw e;
        }
    }

    /**
     * 데이터 유효성 검증
     */
    private boolean isValidData(String admCd, String admNm) {
        return admCd != null 
            && !admCd.isEmpty()
            && !admCd.equals("null")
            && admNm != null 
            && !admNm.isEmpty();
    }

    /**
     * 행정동코드 파싱
     */
    private Integer parseAdminDongCode(String admCd) {
        if (admCd == null || admCd.isEmpty()) {
            return null;
        }
        
        try {
            return Integer.parseInt(admCd);
        } catch (NumberFormatException e) {
            log.warn("행정동코드 파싱 실패: {}", admCd);
            return null;
        }
    }

    /**
     * 금액 파싱 (천만원 단위)
     */
    private Long parseAmount(String amount) {
        if (amount == null || amount.isEmpty()) {
            return 0L;
        }
        
        try {
            // "2,728" -> 2728
            String cleanAmount = amount.replaceAll("[^0-9]", "");
            if (cleanAmount.isEmpty()) {
                return 0L;
            }
            
            return Long.parseLong(cleanAmount);
            
        } catch (NumberFormatException e) {
            log.warn("금액 파싱 실패: {}", amount);
            return 0L;
        }
    }

    /**
     * DB에 소득/소비 데이터 업데이트
     */
    private void updateIncomeConsumptionData(Map<Integer, IncomeConsumptionData> adminDongDataMap, 
                                           PublicDataAnalysisRespDto.CollectionProgressResp progress) {
        
        progress.setTotalRecords(adminDongDataMap.size());
        int processedCount = 0;
        
        for (Map.Entry<Integer, IncomeConsumptionData> entry : adminDongDataMap.entrySet()) {
            Integer adminDongCode = entry.getKey();
            IncomeConsumptionData data = entry.getValue();
            
            try {
                Optional<PublicDataAnalysis> existingOpt = repository.findByAdminDongCode(adminDongCode);
                
                if (existingOpt.isPresent()) {
                    PublicDataAnalysis analysis = existingOpt.get();
                    analysis.updateIncomeAndConsumption(data.getIncome(), data.getConsumption());
                    repository.save(analysis);
                    progress.setSuccessRecords(progress.getSuccessRecords() + 1);
                } else {
                    // 기존 레코드가 없으면 경고 로그
                    log.warn("기존 레코드가 없어 소득/소비 데이터를 업데이트할 수 없습니다. AdminDongCode: {}", adminDongCode);
                    progress.getWarnings().add(String.format("기존 레코드 없음 - %d", adminDongCode));
                    progress.setFailureRecords(progress.getFailureRecords() + 1);
                }
                
            } catch (Exception e) {
                log.error("소득/소비 데이터 업데이트 실패 - AdminDongCode: {}", adminDongCode, e);
                progress.setFailureRecords(progress.getFailureRecords() + 1);
                progress.getErrorMessages().add(String.format("데이터 업데이트 실패 - %d: %s", 
                        adminDongCode, e.getMessage()));
            }
            
            processedCount++;
            progress.setProcessedRecords(processedCount);
        }
    }

    /**
     * 소득/소비 데이터 내부 클래스
     */
    @lombok.Data
    @lombok.Builder
    private static class IncomeConsumptionData {
        private Integer adminDongCode;
        private String adminDongName;
        private Long income;
        private Long consumption;
    }
}