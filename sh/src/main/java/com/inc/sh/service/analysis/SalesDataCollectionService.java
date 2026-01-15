package com.inc.sh.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inc.sh.config.CityBoxCoordinates;
import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.entity.PublicDataAnalysis;
import com.inc.sh.repository.PublicDataAnalysisRepository;

import lombok.Builder;
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
 * 매출 데이터 수집 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class SalesDataCollectionService {

    private final PublicDataAnalysisRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${public-api.sales.base-url:https://bigdata.sbiz.or.kr/gis/api/getMapRadsSaleAmt.json}")
    private String salesApiBaseUrl;

    @Value("${public-api.common.params.mapLevel:3}")
    private String mapLevel;

    @Value("${public-api.common.params.substr:8}")
    private String substr;

    @Value("${public-api.common.params.bzznType:1}")
    private String bzznType;

    /**
     * 매출 데이터 수집 메인 메서드
     */
    public PublicDataAnalysisRespDto.CollectionProgressResp collectSalesData(
            PublicDataAnalysisReqDto.SalesDataCollectReq request) {

        log.info("매출 데이터 수집 시작 - 대상 광역시: {}", request.getCities());

        String taskId = "sales_" + System.currentTimeMillis();
        
        PublicDataAnalysisRespDto.CollectionProgressResp progress = PublicDataAnalysisRespDto.CollectionProgressResp.builder()
                .taskId(taskId)
                .status("PROCESSING")
                .currentStep("SALES")
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

        // 전체 API 호출 수 계산
        int totalApiCalls = request.getCities().stream()
                .mapToInt(city -> {
                    List<CityBoxCoordinates.BoxCoordinate> boxes = CityBoxCoordinates.getBoxes(city);
                    return boxes.size() * CityBoxCoordinates.UPJONG_CODES_SALES.length;
                })
                .sum();
        
        progress.setTotalApiCalls(totalApiCalls);

        try {
            // 중복 제거를 위한 맵 (행정동코드 -> 매출 데이터)
            Map<Integer, Map<String, SalesData>> adminDongSalesMap = new ConcurrentHashMap<>();

            for (String city : request.getCities()) {
                progress.setCurrentCity(city);
                log.info("광역시 {} 매출 데이터 수집 시작", city);

                List<CityBoxCoordinates.BoxCoordinate> boxes = CityBoxCoordinates.getBoxes(city);
                
                for (CityBoxCoordinates.BoxCoordinate box : boxes) {
                    for (String upjongCd : CityBoxCoordinates.UPJONG_CODES_SALES) {
                        
                        try {
                            // API 호출
                            String apiUrl = buildSalesApiUrl(box, upjongCd);
                            List<PublicDataAnalysisRespDto.SalesApiResp> apiResponse = callSalesApi(apiUrl);
                            
                            // 응답 데이터 처리
                            for (PublicDataAnalysisRespDto.SalesApiResp salesResp : apiResponse) {
                                if (isValidSalesData(salesResp)) {
                                    Integer adminDongCode = parseAdminDongCode(salesResp.getAdmCd());
                                    
                                    adminDongSalesMap.computeIfAbsent(adminDongCode, k -> new HashMap<>());
                                    
                                    SalesData salesData = SalesData.builder()
                                            .adminDongCode(adminDongCode)
                                            .adminDongName(salesResp.getAdmNm())
                                            .saleAmt(parseSaleAmount(salesResp.getSaleAmt()))
                                            .storeCnt(parseStoreCount(salesResp.getStoreCnt()))
                                            .build();
                                    
                                    // 배수 적용
                                    if (request.getApplyMultiplier()) {
                                        Double multiplier = CityBoxCoordinates.SALES_MULTIPLIER.get(upjongCd);
                                        if (multiplier != null) {
                                            salesData.setSaleAmt((long) (salesData.getSaleAmt() * multiplier));
                                        }
                                    }
                                    
                                    adminDongSalesMap.get(adminDongCode).put(upjongCd, salesData);
                                }
                            }
                            
                            progress.setSuccessApiCalls(progress.getSuccessApiCalls() + 1);
                            
                            // 지연 시간
                            if (request.getDelayMs() > 0) {
                                Thread.sleep(request.getDelayMs());
                            }
                            
                        } catch (Exception e) {
                            log.error("API 호출 실패 - City: {}, Box: {}, UpjongCd: {}, Error: {}", 
                                    city, box.toString(), upjongCd, e.getMessage(), e);
                            progress.setFailureApiCalls(progress.getFailureApiCalls() + 1);
                            progress.getErrorMessages().add(String.format("API 호출 실패 - %s, %s: %s", 
                                    city, upjongCd, e.getMessage()));
                        }
                        
                        progress.setCompletedApiCalls(progress.getCompletedApiCalls() + 1);
                        
                        // 진행률 업데이트
                        double progressPercent = (double) progress.getCompletedApiCalls() / totalApiCalls * 100;
                        progress.setProgressPercent(progressPercent);
                    }
                }
                
                log.info("광역시 {} 매출 데이터 수집 완료", city);
            }

            // DB에 저장
            saveOrUpdateSalesData(adminDongSalesMap, progress);

            progress.setStatus("COMPLETED");
            progress.setCompletedSteps(1);
            progress.setEndTime(LocalDateTime.now());
            
            if (progress.getStartTime() != null) {
                progress.setElapsedTimeMs(
                    java.time.Duration.between(progress.getStartTime(), progress.getEndTime()).toMillis()
                );
            }
            
            log.info("매출 데이터 수집 완료 - 성공: {}개, 실패: {}개", 
                    progress.getSuccessRecords(), progress.getFailureRecords());

        } catch (Exception e) {
            log.error("매출 데이터 수집 중 오류 발생", e);
            progress.setStatus("FAILED");
            progress.setEndTime(LocalDateTime.now());
            progress.getErrorMessages().add("전체 수집 프로세스 오류: " + e.getMessage());
        }

        return progress;
    }

    /**
     * 매출 API URL 생성
     */
    private String buildSalesApiUrl(CityBoxCoordinates.BoxCoordinate box, String upjongCd) {
        return String.format("%s?mapLevel=%s&substr=%s&%s&upjongCd=%s&bzznType=%s",
                salesApiBaseUrl, mapLevel, substr, box.toUrlParams(), upjongCd, bzznType);
    }

    /**
     * 매출 API 호출
     */
    @SuppressWarnings("unchecked")
    private List<PublicDataAnalysisRespDto.SalesApiResp> callSalesApi(String apiUrl) throws Exception {
        log.debug("매출 API 호출: {}", apiUrl);
        
        try {
            Object response = restTemplate.getForObject(apiUrl, Object.class);
            
            if (response instanceof List) {
                List<Map<String, Object>> responseList = (List<Map<String, Object>>) response;
                List<PublicDataAnalysisRespDto.SalesApiResp> salesList = new ArrayList<>();
                
                for (Map<String, Object> item : responseList) {
                    PublicDataAnalysisRespDto.SalesApiResp salesResp = PublicDataAnalysisRespDto.SalesApiResp.builder()
                            .storeCnt(String.valueOf(item.get("storeCnt")))
                            .saleAmt(String.valueOf(item.get("saleAmt")))
                            .admCd(String.valueOf(item.get("admCd")))
                            .admNm(String.valueOf(item.get("admNm")))
                            .build();
                    
                    salesList.add(salesResp);
                }
                
                return salesList;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("매출 API 호출 실패: {}", apiUrl, e);
            throw e;
        }
    }

    /**
     * 매출 데이터 유효성 검증
     */
    private boolean isValidSalesData(PublicDataAnalysisRespDto.SalesApiResp salesResp) {
        return salesResp != null 
            && salesResp.getAdmCd() != null 
            && !salesResp.getAdmCd().isEmpty()
            && !salesResp.getAdmCd().equals("null")
            && salesResp.getAdmNm() != null 
            && !salesResp.getAdmNm().isEmpty();
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
     * 매출액 파싱 (천만원 단위를 원 단위로 변환)
     */
    private Long parseSaleAmount(String saleAmt) {
        if (saleAmt == null || saleAmt.isEmpty()) {
            return 0L;
        }
        
        try {
            // "3,451 만원" -> 34510000
            String cleanAmount = saleAmt.replaceAll("[^0-9,]", "").replaceAll(",", "");
            if (cleanAmount.isEmpty()) {
                return 0L;
            }
            
            Long amount = Long.parseLong(cleanAmount);
            return amount * 10000; // 만원을 원으로 변환
            
        } catch (NumberFormatException e) {
            log.warn("매출액 파싱 실패: {}", saleAmt);
            return 0L;
        }
    }

    /**
     * 업소수 파싱
     */
    private Integer parseStoreCount(String storeCnt) {
        if (storeCnt == null || storeCnt.isEmpty()) {
            return 0;
        }
        
        try {
            return Integer.parseInt(storeCnt);
        } catch (NumberFormatException e) {
            log.warn("업소수 파싱 실패: {}", storeCnt);
            return 0;
        }
    }

    /**
     * DB에 매출 데이터 저장 또는 업데이트
     */
    private void saveOrUpdateSalesData(Map<Integer, Map<String, SalesData>> adminDongSalesMap, 
                                     PublicDataAnalysisRespDto.CollectionProgressResp progress) {
        
        progress.setTotalRecords(adminDongSalesMap.size());
        int processedCount = 0;
        
        for (Map.Entry<Integer, Map<String, SalesData>> entry : adminDongSalesMap.entrySet()) {
            Integer adminDongCode = entry.getKey();
            Map<String, SalesData> salesByUpjong = entry.getValue();
            
            try {
                Optional<PublicDataAnalysis> existingOpt = repository.findByAdminDongCode(adminDongCode);
                PublicDataAnalysis analysis;
                
                if (existingOpt.isPresent()) {
                    analysis = existingOpt.get();
                } else {
                    // 새로운 레코드 생성
                    String adminDongName = salesByUpjong.values().iterator().next().getAdminDongName();
                    analysis = PublicDataAnalysis.builder()
                            .adminDongCode(adminDongCode)
                            .adminDongName(adminDongName)
                            .build();
                }
                
                // 업종별 매출 데이터 업데이트
                for (Map.Entry<String, SalesData> salesEntry : salesByUpjong.entrySet()) {
                    String upjongCd = salesEntry.getKey();
                    SalesData salesData = salesEntry.getValue();
                    
                    analysis.updateSalesData(upjongCd, salesData.getStoreCnt(), salesData.getSaleAmt());
                }
                
                repository.save(analysis);
                progress.setSuccessRecords(progress.getSuccessRecords() + 1);
                
            } catch (Exception e) {
                log.error("매출 데이터 저장 실패 - AdminDongCode: {}", adminDongCode, e);
                progress.setFailureRecords(progress.getFailureRecords() + 1);
                progress.getErrorMessages().add(String.format("데이터 저장 실패 - %d: %s", 
                        adminDongCode, e.getMessage()));
            }
            
            processedCount++;
            progress.setProcessedRecords(processedCount);
        }
    }

    /**
     * 매출 데이터 내부 클래스
     */
    @lombok.Data
    @Builder
    private static class SalesData {
        private Integer adminDongCode;
        private String adminDongName;
        private Long saleAmt;
        private Integer storeCnt;
    }
}