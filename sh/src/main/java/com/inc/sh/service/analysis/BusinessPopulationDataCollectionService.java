package com.inc.sh.service.analysis;

import com.inc.sh.dto.publicDataAnalysis.reqDto.PublicDataAnalysisReqDto;
import com.inc.sh.dto.publicDataAnalysis.respDto.PublicDataAnalysisRespDto;
import com.inc.sh.entity.PublicDataAnalysis;
import com.inc.sh.repository.PublicDataAnalysisRepository;
import com.inc.sh.config.CityBoxCoordinates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 업소수/인구 데이터 수집 서비스
 * 
 * 🎯 수정된 정책:
 * 1. API 파라미터는 기본값 유지 (areaGb=1&areaDiv=1) - 추측으로 변경 안함
 * 2. 응답 데이터에서 동 단위(8자리 이상)만 필터링
 * 3. 기존 매출 레코드가 있는 경우에만 UPDATE
 * 4. 새로운 레코드 생성하지 않음 (시/구 단위 중복 방지)
 */
@Slf4j
@Service
public class BusinessPopulationDataCollectionService {

    private final PublicDataAnalysisRepository repository;
    private RestTemplate restTemplate;
    
    @Value("${public-api.business-population.base-url:}")
    private String businessPopulationApiBaseUrl;
    
    @Value("${public-api.household-residential.base-url:}")
    private String householdResidentialApiBaseUrl;
    
    @Value("${public-api.working-population.base-url:}")
    private String workingPopulationApiBaseUrl;

    public BusinessPopulationDataCollectionService(PublicDataAnalysisRepository repository) {
        this.repository = repository;
        this.restTemplate = new RestTemplate();
    }

    // 데이터 수집을 위한 내부 클래스들
    private static class BusinessPopulationData {
        private Integer adminDongCode;
        private String adminDongName;
        private Integer koreanRestaurantCount;
        private Integer chineseRestaurantCount;
        private Integer japaneseRestaurantCount;
        private Integer westernRestaurantCount;
        private Integer southeastAsianRestaurantCount;
        private Integer floatingPopulation;
        private Integer residentialPopulation;
        private Integer workingPopulation;
        private Integer householdCount;

        public static BusinessPopulationDataBuilder builder() {
            return new BusinessPopulationDataBuilder();
        }

        public static class BusinessPopulationDataBuilder {
            private final BusinessPopulationData data = new BusinessPopulationData();
            
            public BusinessPopulationDataBuilder adminDongCode(Integer adminDongCode) {
                data.adminDongCode = adminDongCode;
                return this;
            }
            
            public BusinessPopulationDataBuilder adminDongName(String adminDongName) {
                data.adminDongName = adminDongName;
                return this;
            }
            
            public BusinessPopulationData build() {
                return data;
            }
        }

        public void updateBusinessCount(String upjongCd, Integer count) {
            switch (upjongCd) {
                case "I201": this.koreanRestaurantCount = count; break;
                case "I202": this.chineseRestaurantCount = count; break;
                case "I203": this.japaneseRestaurantCount = count; break;
                case "I204": this.westernRestaurantCount = count; break;
                case "I205": this.southeastAsianRestaurantCount = count; break;
            }
        }

        // Getters and Setters
        public Integer getAdminDongCode() { return adminDongCode; }
        public String getAdminDongName() { return adminDongName; }
        public Integer getKoreanRestaurantCount() { return koreanRestaurantCount; }
        public Integer getChineseRestaurantCount() { return chineseRestaurantCount; }
        public Integer getJapaneseRestaurantCount() { return japaneseRestaurantCount; }
        public Integer getWesternRestaurantCount() { return westernRestaurantCount; }
        public Integer getSoutheastAsianRestaurantCount() { return southeastAsianRestaurantCount; }
        public Integer getFloatingPopulation() { return floatingPopulation; }
        public Integer getResidentialPopulation() { return residentialPopulation; }
        public Integer getWorkingPopulation() { return workingPopulation; }
        public Integer getHouseholdCount() { return householdCount; }

        public void setFloatingPopulation(Integer floatingPopulation) { this.floatingPopulation = floatingPopulation; }
        public void setResidentialPopulation(Integer residentialPopulation) { this.residentialPopulation = residentialPopulation; }
        public void setWorkingPopulation(Integer workingPopulation) { this.workingPopulation = workingPopulation; }
        public void setHouseholdCount(Integer householdCount) { this.householdCount = householdCount; }
    }

    // API 응답 클래스들
    private static class BusinessPopulationApiResponse {
        private List<PublicDataAnalysisRespDto.DynpplSttusApiResp> dynpplStatsList;
        public List<PublicDataAnalysisRespDto.DynpplSttusApiResp> getDynpplStatsList() { return dynpplStatsList; }
        public void setDynpplStatsList(List<PublicDataAnalysisRespDto.DynpplSttusApiResp> dynpplStatsList) { this.dynpplStatsList = dynpplStatsList; }
    }

    private static class HouseholdResidentialApiResponse {
        private List<PublicDataAnalysisRespDto.RgnSttusApiResp> rgnStatsList;
        public List<PublicDataAnalysisRespDto.RgnSttusApiResp> getRgnStatsList() { return rgnStatsList; }
        public void setRgnStatsList(List<PublicDataAnalysisRespDto.RgnSttusApiResp> rgnStatsList) { this.rgnStatsList = rgnStatsList; }
    }

    private static class WorkingPopulationApiResponse {
        private List<PublicDataAnalysisRespDto.WrcpplSttusApiResp> wrcpplStatsList;
        public List<PublicDataAnalysisRespDto.WrcpplSttusApiResp> getWrcpplStatsList() { return wrcpplStatsList; }
        public void setWrcpplStatsList(List<PublicDataAnalysisRespDto.WrcpplSttusApiResp> wrcpplStatsList) { this.wrcpplStatsList = wrcpplStatsList; }
    }

    /**
     * 업소수/인구 데이터 수집 메인 메서드
     */
    public PublicDataAnalysisRespDto.CollectionProgressResp collectBusinessPopulationData(
            PublicDataAnalysisReqDto.BusinessPopulationDataCollectReq request) {

        String taskId = "business_population_" + System.currentTimeMillis();
        PublicDataAnalysisRespDto.CollectionProgressResp progress = 
                PublicDataAnalysisRespDto.CollectionProgressResp.builder()
                        .taskId(taskId)
                        .status("IN_PROGRESS")
                        .currentStep("BUSINESS_POPULATION")
                        .totalSteps(1)
                        .completedSteps(0)
                        .startTime(LocalDateTime.now())
                        .totalApiCalls(0)
                        .completedApiCalls(0)
                        .successApiCalls(0)
                        .failureApiCalls(0)
                        .totalRecords(0)
                        .processedRecords(0)
                        .successRecords(0)
                        .failureRecords(0)
                        .progressPercent(0.0)
                        .errorMessages(new ArrayList<>())
                        .warnings(new ArrayList<>())
                        .build();

        log.info("업소수/인구 데이터 수집 시작 - 대상 지역코드: {}개", request.getAreaCodes().size());

        // 동 단위 기존 레코드만 업데이트하기 위한 Map
        Map<Integer, BusinessPopulationData> dongDataMap = new HashMap<>();
        
        // API 호출 총 개수 계산
        int totalApiCalls = request.getAreaCodes().size() * 
            (request.getIncludeBusinessCount() ? CityBoxCoordinates.UPJONG_CODES_BUSINESS.length : 0) +
            request.getAreaCodes().size() * (request.getIncludePopulationData() ? 2 : 0);
        progress.setTotalApiCalls(totalApiCalls);

        try {
            for (String areaCd : request.getAreaCodes()) {
                progress.setCurrentAreaCode(areaCd);
                log.info("지역코드 {} 데이터 수집 시작", areaCd);

                // 1. 업소수 및 유동인구 수집
                if (request.getIncludeBusinessCount()) {
                    for (String upjongCd : CityBoxCoordinates.UPJONG_CODES_BUSINESS) {
                        try {
                            String apiUrl = buildBusinessPopulationApiUrl(areaCd, upjongCd);
                            BusinessPopulationApiResponse apiResponse = callBusinessPopulationApi(apiUrl);
                            
                            if (apiResponse != null && apiResponse.getDynpplStatsList() != null) {
                                log.info("🔍 업소수 API 응답 데이터 개수: {}, areaCd: {}, upjongCd: {}", 
                                        apiResponse.getDynpplStatsList().size(), areaCd, upjongCd);
                                processBusinessPopulationResponse(apiResponse.getDynpplStatsList(), upjongCd, dongDataMap);
                            } else {
                                log.warn("❌ 업소수 API 응답 null 또는 빈 데이터: areaCd={}, upjongCd={}", areaCd, upjongCd);
                            }
                            
                            progress.setSuccessApiCalls((progress.getSuccessApiCalls() == null ? 0 : progress.getSuccessApiCalls()) + 1);
                            
                        } catch (Exception e) {
                            log.error("업소수/유동인구 API 호출 실패 - AreaCd: {}, UpjongCd: {}", areaCd, upjongCd, e);
                            progress.setFailureApiCalls((progress.getFailureApiCalls() == null ? 0 : progress.getFailureApiCalls()) + 1);
                        }
                        
                        progress.setCompletedApiCalls((progress.getCompletedApiCalls() == null ? 0 : progress.getCompletedApiCalls()) + 1);
                    }
                }

                // 2. 세대수 및 주거인구 수집
                if (request.getIncludePopulationData()) {
                    try {
                        String apiUrl = buildHouseholdResidentialApiUrl(areaCd);
                        HouseholdResidentialApiResponse apiResponse = callHouseholdResidentialApi(apiUrl);
                        
                        if (apiResponse != null && apiResponse.getRgnStatsList() != null) {
                            processHouseholdResidentialResponse(apiResponse.getRgnStatsList(), dongDataMap);
                        }
                        progress.setSuccessApiCalls((progress.getSuccessApiCalls() == null ? 0 : progress.getSuccessApiCalls()) + 1);
                        
                    } catch (Exception e) {
                        log.error("세대/주거인구 API 호출 실패 - AreaCd: {}", areaCd, e);
                        progress.setFailureApiCalls((progress.getFailureApiCalls() == null ? 0 : progress.getFailureApiCalls()) + 1);
                    }
                    progress.setCompletedApiCalls((progress.getCompletedApiCalls() == null ? 0 : progress.getCompletedApiCalls()) + 1);

                    // 3. 직장인구 수집
                    try {
                        String apiUrl = buildWorkingPopulationApiUrl(areaCd);
                        WorkingPopulationApiResponse apiResponse = callWorkingPopulationApi(apiUrl);
                        
                        if (apiResponse != null && apiResponse.getWrcpplStatsList() != null) {
                            processWorkingPopulationResponse(apiResponse.getWrcpplStatsList(), dongDataMap);
                        }
                        progress.setSuccessApiCalls((progress.getSuccessApiCalls() == null ? 0 : progress.getSuccessApiCalls()) + 1);
                        
                    } catch (Exception e) {
                        log.error("직장인구 API 호출 실패 - AreaCd: {}", areaCd, e);
                        progress.setFailureApiCalls((progress.getFailureApiCalls() == null ? 0 : progress.getFailureApiCalls()) + 1);
                    }
                    progress.setCompletedApiCalls((progress.getCompletedApiCalls() == null ? 0 : progress.getCompletedApiCalls()) + 1);
                }
            }

            // 4. 기존 매출 레코드만 업데이트
            updateOnlyExistingRecords(dongDataMap, progress);

            progress.setStatus("COMPLETED");
            progress.setCompletedSteps(1);
            
        } catch (Exception e) {
            log.error("업소수/인구 데이터 수집 중 오류 발생", e);
            progress.setStatus("FAILED");
            progress.getErrorMessages().add("수집 실패: " + e.getMessage());
        }

        progress.setEndTime(LocalDateTime.now());
        int completedCalls = progress.getCompletedApiCalls() == null ? 0 : progress.getCompletedApiCalls();
        int totalCalls = progress.getTotalApiCalls() == null ? 1 : progress.getTotalApiCalls();
        progress.setProgressPercent((double) completedCalls / totalCalls * 100);

        log.info("업소수/인구 데이터 수집 완료 - 성공: {}개, 실패: {}개", 
                progress.getSuccessRecords(), progress.getFailureRecords());

        return progress;
    }

    /**
     * 🎯 핵심 로직: 업소수 API 응답 처리 - 동 단위만, 기존 레코드만
     */
    private void processBusinessPopulationResponse(List<PublicDataAnalysisRespDto.DynpplSttusApiResp> responseList, 
                                                  String upjongCd,
                                                  Map<Integer, BusinessPopulationData> dongDataMap) {
        
        log.info("🔍 processBusinessPopulationResponse 시작 - 응답 데이터: {}개, 업종: {}", 
                responseList.size(), upjongCd);
        
        for (PublicDataAnalysisRespDto.DynpplSttusApiResp resp : responseList) {
            log.debug("🔍 API 응답 데이터: areaCd='{}', areaNm='{}', upsCnt='{}', dynPopnum='{}'", 
                    resp.getAreaCd(), resp.getAreaNm(), resp.getUpsoCnt(), resp.getDynPopnum());
            
            if (!isValidAreaData(resp.getAreaCd(), resp.getAreaNm())) {
                log.warn("❌ 유효하지 않은 데이터 - areaCd: {}, areaNm: {}", resp.getAreaCd(), resp.getAreaNm());
                continue;
            }

            Integer adminDongCode = parseAdminDongCode(resp.getAreaCd());
            if (adminDongCode == null) {
                log.warn("❌ 행정동코드 파싱 실패: {}", resp.getAreaCd());
                continue;
            }

            log.info("🔍 파싱된 행정동코드: {} (길이: {})", adminDongCode, String.valueOf(adminDongCode).length());
            
            // 🎯 동 단위만 처리 (8자리 이상)
            if (String.valueOf(adminDongCode).length() >= 8) {
                
                // 🎯 기존 매출 레코드가 있는지 확인
                Optional<PublicDataAnalysis> existingOpt = repository.findByAdminDongCode(adminDongCode);
                
                if (existingOpt.isPresent()) {
                    
                    dongDataMap.computeIfAbsent(adminDongCode, k -> 
                        BusinessPopulationData.builder()
                                .adminDongCode(adminDongCode)
                                .adminDongName(resp.getAreaNm())
                                .build());
                    
                    BusinessPopulationData data = dongDataMap.get(adminDongCode);
                    data.updateBusinessCount(upjongCd, parseInteger(resp.getUpsoCnt()));
                    
                    if (data.getFloatingPopulation() == null || data.getFloatingPopulation() == 0) {
                        data.setFloatingPopulation(parseInteger(resp.getDynPopnum()));
                    }
                    
                } else {
                    log.info("❌ 기존 매출 레코드 없음 - 건너뜀: {} ({})", adminDongCode, resp.getAreaNm());
                }
            } else {
                log.info("❌ 시/구 단위 데이터 - 건너뜀: {} (길이: {})", 
                        adminDongCode, String.valueOf(adminDongCode).length());
            }
        }
        
        log.info("🔍 processBusinessPopulationResponse 완료 - 최종 dongDataMap 크기: {}", dongDataMap.size());
    }

    /**
     * 세대/주거인구 API 응답 처리
     */
    private void processHouseholdResidentialResponse(List<PublicDataAnalysisRespDto.RgnSttusApiResp> responseList,
                                                   Map<Integer, BusinessPopulationData> dongDataMap) {
        
        for (PublicDataAnalysisRespDto.RgnSttusApiResp resp : responseList) {
            if (!isValidAreaData(resp.getAreaCd(), resp.getAreaNm())) {
                continue;
            }

            Integer adminDongCode = parseAdminDongCode(resp.getAreaCd());
            if (adminDongCode == null || String.valueOf(adminDongCode).length() < 8) {
                continue;
            }

            // 기존 매출 레코드가 있는지 확인
            if (repository.findByAdminDongCode(adminDongCode).isPresent()) {
                dongDataMap.computeIfAbsent(adminDongCode, k -> 
                    BusinessPopulationData.builder()
                            .adminDongCode(adminDongCode)
                            .adminDongName(resp.getAreaNm())
                            .build());
                
                BusinessPopulationData data = dongDataMap.get(adminDongCode);
                data.setHouseholdCount(parseInteger(resp.getHous()));
                data.setResidentialPopulation(parseInteger(resp.getPop()));
            }
        }
    }

    /**
     * 직장인구 API 응답 처리
     */
    private void processWorkingPopulationResponse(List<PublicDataAnalysisRespDto.WrcpplSttusApiResp> responseList,
                                                Map<Integer, BusinessPopulationData> dongDataMap) {
        
        for (PublicDataAnalysisRespDto.WrcpplSttusApiResp resp : responseList) {
            if (!isValidAreaData(resp.getAreaCd(), resp.getAreaNm())) {
                continue;
            }

            Integer adminDongCode = parseAdminDongCode(resp.getAreaCd());
            if (adminDongCode == null || String.valueOf(adminDongCode).length() < 8) {
                continue;
            }

            // 기존 매출 레코드가 있는지 확인
            if (repository.findByAdminDongCode(adminDongCode).isPresent()) {
                dongDataMap.computeIfAbsent(adminDongCode, k -> 
                    BusinessPopulationData.builder()
                            .adminDongCode(adminDongCode)
                            .adminDongName(resp.getAreaNm())
                            .build());
                
                BusinessPopulationData data = dongDataMap.get(adminDongCode);
                data.setWorkingPopulation(parseInteger(resp.getWrcPopnum()));
            }
        }
    }

    /**
     * 🎯 핵심 로직: 기존 매출 레코드만 업데이트, 새 레코드 생성 안함
     */
    private void updateOnlyExistingRecords(Map<Integer, BusinessPopulationData> dongDataMap, 
                                         PublicDataAnalysisRespDto.CollectionProgressResp progress) {
        
        log.info("🎯 기존 매출 레코드 업데이트 시작 - 대상 동: {}개", dongDataMap.size());
        progress.setTotalRecords(dongDataMap.size());
        
        for (Map.Entry<Integer, BusinessPopulationData> entry : dongDataMap.entrySet()) {
            Integer adminDongCode = entry.getKey();
            BusinessPopulationData data = entry.getValue();
            
            try {
                Optional<PublicDataAnalysis> existingOpt = repository.findByAdminDongCode(adminDongCode);
                
                if (existingOpt.isPresent()) {
                    PublicDataAnalysis analysis = existingOpt.get();
                    
                    // 업소수 업데이트
                    updateBusinessCounts(analysis, data);
                    
                    // 인구 데이터 업데이트
                    analysis.updatePopulationData(
                            data.getFloatingPopulation(),
                            data.getResidentialPopulation(),
                            data.getWorkingPopulation(),
                            data.getHouseholdCount()
                    );
                    
                    repository.save(analysis);
                    progress.setSuccessRecords((progress.getSuccessRecords() == null ? 0 : progress.getSuccessRecords()) + 1);
                    
                } else {
                    // 이 경우는 발생하지 않아야 함 (이미 필터링했으므로)
                    log.warn("⚠️ 예상치 못한 상황: 기존 레코드가 없습니다 - {}", adminDongCode);
                    progress.setFailureRecords((progress.getFailureRecords() == null ? 0 : progress.getFailureRecords()) + 1);
                }
                
            } catch (Exception e) {
                log.error("❌ 레코드 업데이트 실패 - AdminDongCode: {}", adminDongCode, e);
                progress.setFailureRecords((progress.getFailureRecords() == null ? 0 : progress.getFailureRecords()) + 1);
            }
            
            progress.setProcessedRecords((progress.getProcessedRecords() == null ? 0 : progress.getProcessedRecords()) + 1);
        }
        
    }

    private void updateBusinessCounts(PublicDataAnalysis analysis, BusinessPopulationData data) {
        if (data.getKoreanRestaurantCount() != null) {
            analysis.updateBusinessCount("I201", data.getKoreanRestaurantCount());
        }
        if (data.getChineseRestaurantCount() != null) {
            analysis.updateBusinessCount("I202", data.getChineseRestaurantCount());
        }
        if (data.getJapaneseRestaurantCount() != null) {
            analysis.updateBusinessCount("I203", data.getJapaneseRestaurantCount());
        }
        if (data.getWesternRestaurantCount() != null) {
            analysis.updateBusinessCount("I204", data.getWesternRestaurantCount());
        }
        if (data.getSoutheastAsianRestaurantCount() != null) {
            analysis.updateBusinessCount("I205", data.getSoutheastAsianRestaurantCount());
        }
    }

    // API URL 생성 메서드들 - 기본 파라미터 유지 (추측으로 변경 안함)
    private String buildBusinessPopulationApiUrl(String areaCd, String upjongCd) {
        if (businessPopulationApiBaseUrl == null || businessPopulationApiBaseUrl.isEmpty()) {
            log.error("❌ businessPopulationApiBaseUrl이 설정되지 않았습니다!");
            return "";
        }
        String url = String.format("%s?areaCd=%s&areaGb=1&areaDiv=1&upjongCd=%s&upjongGb=2&sprTypeNo=1",
                businessPopulationApiBaseUrl, areaCd, upjongCd);
        log.debug("🔍 생성된 API URL: {}", url);
        return url;
    }

    private String buildHouseholdResidentialApiUrl(String areaCd) {
        return String.format("%s?areaCd=%s&areaGb=1&areaDiv=1&upjongCd=&upjongGb=0&sprTypeNo=1",
                householdResidentialApiBaseUrl, areaCd);
    }

    private String buildWorkingPopulationApiUrl(String areaCd) {
        return String.format("%s?areaCd=%s&areaGb=1&areaDiv=1&upjongCd=&upjongGb=0&sprTypeNo=1",
                workingPopulationApiBaseUrl, areaCd);
    }

    // API 호출 메서드들
    private BusinessPopulationApiResponse callBusinessPopulationApi(String apiUrl) throws Exception {
        log.info("🔍 API 호출: {}", apiUrl);
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        
        log.info("🔍 API 원본 응답: {}", response);
        
        if (response != null && "SUCCESS".equals(response.get("resultCode"))) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                BusinessPopulationApiResponse apiResponse = new BusinessPopulationApiResponse();
                
                List<Map<String, Object>> dynpplStatsList = (List<Map<String, Object>>) data.get("dynpplStatsList");
                log.info("🔍 dynpplStatsList 크기: {}", dynpplStatsList != null ? dynpplStatsList.size() : "null");
                
                if (dynpplStatsList != null) {
                    List<PublicDataAnalysisRespDto.DynpplSttusApiResp> respList = new ArrayList<>();
                    for (Map<String, Object> item : dynpplStatsList) {
                        log.debug("🔍 원본 item: {}", item);
                        PublicDataAnalysisRespDto.DynpplSttusApiResp resp = PublicDataAnalysisRespDto.DynpplSttusApiResp.builder()
                                .areaNm(String.valueOf(item.get("areaNm")))
                                .areaGb(String.valueOf(item.get("areaGb")))
                                .areaCd(String.valueOf(item.get("areaCd")))
                                .upsoCnt(String.valueOf(item.get("upsoCnt")))
                                .dynPopnum(String.valueOf(item.get("dynPopnum")))
                                .build();
                        respList.add(resp);
                    }
                    apiResponse.setDynpplStatsList(respList);
                }
                
                return apiResponse;
            }
        } else {
            log.warn("❌ API 호출 실패 또는 resultCode != SUCCESS: {}", response != null ? response.get("resultCode") : "null response");
        }
        return null;
    }

    private HouseholdResidentialApiResponse callHouseholdResidentialApi(String apiUrl) throws Exception {
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        
        if (response != null && "SUCCESS".equals(response.get("resultCode"))) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                HouseholdResidentialApiResponse apiResponse = new HouseholdResidentialApiResponse();
                
                List<Map<String, Object>> rgnStatsList = (List<Map<String, Object>>) data.get("rgnStatsList");
                if (rgnStatsList != null) {
                    List<PublicDataAnalysisRespDto.RgnSttusApiResp> respList = new ArrayList<>();
                    for (Map<String, Object> item : rgnStatsList) {
                        PublicDataAnalysisRespDto.RgnSttusApiResp resp = PublicDataAnalysisRespDto.RgnSttusApiResp.builder()
                                .areaNm(String.valueOf(item.get("areaNm")))
                                .areaGb(String.valueOf(item.get("areaGb")))
                                .areaCd(String.valueOf(item.get("areaCd")))
                                .hous(String.valueOf(item.get("hous")))
                                .pop(String.valueOf(item.get("pop")))
                                .upsoCnt(String.valueOf(item.get("upsoCnt")))
                                .build();
                        respList.add(resp);
                    }
                    apiResponse.setRgnStatsList(respList);
                }
                
                return apiResponse;
            }
        }
        return null;
    }

    private WorkingPopulationApiResponse callWorkingPopulationApi(String apiUrl) throws Exception {
        Map<String, Object> response = restTemplate.getForObject(apiUrl, Map.class);
        
        if (response != null && "SUCCESS".equals(response.get("resultCode"))) {
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                WorkingPopulationApiResponse apiResponse = new WorkingPopulationApiResponse();
                
                List<Map<String, Object>> wrcpplStatsList = (List<Map<String, Object>>) data.get("wrcpplStatsList");
                if (wrcpplStatsList != null) {
                    List<PublicDataAnalysisRespDto.WrcpplSttusApiResp> respList = new ArrayList<>();
                    for (Map<String, Object> item : wrcpplStatsList) {
                        PublicDataAnalysisRespDto.WrcpplSttusApiResp resp = PublicDataAnalysisRespDto.WrcpplSttusApiResp.builder()
                                .areaNm(String.valueOf(item.get("areaNm")))
                                .areaCd(String.valueOf(item.get("areaCd")))
                                .areaGb(String.valueOf(item.get("areaGb")))
                                .wrcPopnum(String.valueOf(item.get("wrcPopnum")))
                                .upsoCnt(String.valueOf(item.get("upsoCnt")))
                                .build();
                        respList.add(resp);
                    }
                    apiResponse.setWrcpplStatsList(respList);
                }
                
                return apiResponse;
            }
        }
        return null;
    }

    // 유틸리티 메서드들
    private boolean isValidAreaData(String areaCd, String areaNm) {
        return areaCd != null && !areaCd.trim().isEmpty() && !"null".equals(areaCd) &&
               areaNm != null && !areaNm.trim().isEmpty() && !"null".equals(areaNm);
    }

    private Integer parseAdminDongCode(String areaCd) {
        try {
            return Integer.parseInt(areaCd.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}