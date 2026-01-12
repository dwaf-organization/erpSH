package com.inc.sh.service.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.respDto.HyphenReviewRespDto;
import com.inc.sh.entity.StorePlatforms;
import com.inc.sh.repository.StorePlatformsRepository;
import com.inc.sh.repository.ReviewPlatformRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformReviewService {
    
    private final ObjectMapper objectMapper;
    private final StorePlatformsRepository storePlatformsRepository;
    private final ReviewPlatformRepository reviewPlatformRepository;
    
    // 테스트용 지연 설정 (운영시 false로 변경)
    @Value("${platform.sync.delay.enabled:true}")
    private boolean delayEnabled;
    
    @Value("${platform.sync.delay.seconds:120}")
    private int delaySeconds;
    
    // 하이픈 API 설정
    private static final String HYPHEN_API_URL_BAEMIN = "https://api.hyphen.im/in0022000066";
    private static final String HYPHEN_API_URL_YOGIYO = "https://api.hyphen.im/in0023000077";
    private static final String HYPHEN_API_URL_COUPANG = "https://api.hyphen.im/in0024000800";

    private static final String HKEY = "30cab2cddc0a9352";
    private static final String USER_ID = "shcompany2";
    
    /**
     * 배민 리뷰 동기화
     */
    @Transactional
    public RespDto<Map<String, Object>> syncBaeminReviews(Integer hqCode) {
        try {
            log.info("배민 리뷰 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 해당 본사의 배민 매장 조회
            List<StorePlatforms> authorizedStores = storePlatformsRepository
                    .findByPlatformAndHqCodeAndIsActiveTrue("배민", hqCode);
            
            if (authorizedStores.isEmpty()) {
                log.warn("배민 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("배민 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            // 조회기간 설정 (30일간)
            LocalDate today = LocalDate.now();
            String dateFrom = today.minusDays(30).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            log.info("권한 있는 배민 매장 수: {}", authorizedStores.size());
            
            int totalReviews = 0;
            int savedReviews = 0;
            int duplicateReviews = 0;
            int filteredReviews = 0;
            int totalFailed = 0;
            
            // 2. 각 매장별로 리뷰 조회 및 저장
            for (StorePlatforms store : authorizedStores) {
                try {
                    log.info("매장 리뷰 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출 (배민은 body 없음)
                    HyphenReviewRespDto apiResponse = callHyphenReviewApiBaemin(
                            store.getLoginId(), store.getLoginPassword(), dateFrom, dateTo);
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 리뷰 데이터 처리
                        Map<String, Integer> storeResult = processStoreReviews(store, apiResponse, "배민");
                        totalReviews += storeResult.get("total");
                        savedReviews += storeResult.get("saved");
                        duplicateReviews += storeResult.get("duplicate");
                        filteredReviews += storeResult.get("filtered");
                        
                        // 동기화 시간 업데이트
                        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                        
                        log.info("배민 매장 리뷰 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                store.getStorePlatformCode(), storeResult.get("saved"));
                                
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 리뷰 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                
                // 테스트용: 매장별 지연 처리
                if (delayEnabled && authorizedStores.indexOf(store) < authorizedStores.size() - 1) {
                    try {
                        log.info("🕒 테스트용 지연 시작 - {}초 대기 (매장: {})", delaySeconds, store.getPlatformStoreName());
                        Thread.sleep(delaySeconds * 1000L);
                        log.info("⏰ 테스트용 지연 완료 - 다음 매장 처리 시작");
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("지연 처리 중단됨");
                    }
                }
            }
            
            // 결과 생성
            Map<String, Object> result = new HashMap<>();
            result.put("hqCode", hqCode);
            result.put("platform", "배민");
            result.put("authorizedStoreCount", authorizedStores.size());
            result.put("totalReviewsReceived", totalReviews);
            result.put("savedReviewCount", savedReviews);
            result.put("duplicateReviewCount", duplicateReviews);
            result.put("filteredReviewCount", filteredReviews);
            result.put("failedStoreCount", totalFailed);
            result.put("description", "하이픈 API 연동 - 배민 리뷰 - 본사: " + hqCode);
            
            String resultMessage = String.format("배민 리뷰 동기화 완료 - 총:%d건, 저장:%d건, 중복:%d건, 필터링:%d건, 실패:%d매장", 
                    totalReviews, savedReviews, duplicateReviews, filteredReviews, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, result);
            
        } catch (Exception e) {
            log.error("배민 리뷰 동기화 중 오류 발생", e);
            return RespDto.fail("배민 리뷰 동기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 요기요 리뷰 동기화
     */
    @Transactional
    public RespDto<Map<String, Object>> syncYogiyoReviews(Integer hqCode) {
        try {
            log.info("요기요 리뷰 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 해당 본사의 요기요 매장 조회
            List<StorePlatforms> authorizedStores = storePlatformsRepository
                    .findByPlatformAndHqCodeAndIsActiveTrue("요기요", hqCode);
            
            if (authorizedStores.isEmpty()) {
                log.warn("요기요 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("요기요 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            log.info("권한 있는 요기요 매장 수: {}", authorizedStores.size());
            
            // 조회기간 설정 (30일간)
            LocalDate today = LocalDate.now();
            String dateFrom = today.minusDays(30).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            int totalReviews = 0;
            int savedReviews = 0;
            int duplicateReviews = 0;
            int filteredReviews = 0;
            int totalFailed = 0;
            
            // 2. 각 매장별로 리뷰 조회 및 저장
            for (StorePlatforms store : authorizedStores) {
                try {
                    log.info("매장 리뷰 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출 (요기요)
                    HyphenReviewRespDto apiResponse = callHyphenReviewApiYogiyo(
                            store.getLoginId(), store.getLoginPassword(), dateFrom, dateTo);
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 리뷰 데이터 처리
                        Map<String, Integer> storeResult = processStoreReviews(store, apiResponse, "요기요");
                        totalReviews += storeResult.get("total");
                        savedReviews += storeResult.get("saved");
                        duplicateReviews += storeResult.get("duplicate");
                        filteredReviews += storeResult.get("filtered");
                        
                        // 동기화 시간 업데이트
                        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                        
                        log.info("요기요 매장 리뷰 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                store.getStorePlatformCode(), storeResult.get("saved"));
                                
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 리뷰 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                
                // 테스트용: 매장별 지연 처리
                if (delayEnabled && authorizedStores.indexOf(store) < authorizedStores.size() - 1) {
                    try {
                        log.info("🕒 테스트용 지연 시작 - {}초 대기 (매장: {})", delaySeconds, store.getPlatformStoreName());
                        Thread.sleep(delaySeconds * 1000L);
                        log.info("⏰ 테스트용 지연 완료 - 다음 매장 처리 시작");
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("지연 처리 중단됨");
                    }
                }
            }
            
            // 결과 생성
            Map<String, Object> result = new HashMap<>();
            result.put("hqCode", hqCode);
            result.put("platform", "요기요");
            result.put("authorizedStoreCount", authorizedStores.size());
            result.put("totalReviewsReceived", totalReviews);
            result.put("savedReviewCount", savedReviews);
            result.put("duplicateReviewCount", duplicateReviews);
            result.put("filteredReviewCount", filteredReviews);
            result.put("failedStoreCount", totalFailed);
            result.put("description", "하이픈 API 연동 - 요기요 리뷰 - 본사: " + hqCode);
            
            String resultMessage = String.format("요기요 리뷰 동기화 완료 - 총:%d건, 저장:%d건, 중복:%d건, 필터링:%d건, 실패:%d매장", 
                    totalReviews, savedReviews, duplicateReviews, filteredReviews, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, result);
            
        } catch (Exception e) {
            log.error("요기요 리뷰 동기화 중 오류 발생", e);
            return RespDto.fail("요기요 리뷰 동기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 쿠팡이츠 리뷰 동기화
     */
    @Transactional
    public RespDto<Map<String, Object>> syncCoupangReviews(Integer hqCode) {
        try {
            log.info("쿠팡이츠 리뷰 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 해당 본사의 쿠팡이츠 매장 조회
            List<StorePlatforms> authorizedStores = storePlatformsRepository
                    .findByPlatformAndHqCodeAndIsActiveTrue("쿠팡이츠", hqCode);
            
            if (authorizedStores.isEmpty()) {
                log.warn("쿠팡이츠 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("쿠팡이츠 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            // 조회기간 설정 (30일간)
            LocalDate today = LocalDate.now();
            String dateFrom = today.minusDays(30).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            log.info("권한 있는 쿠팡이츠 매장 수: {}", authorizedStores.size());
            
            int totalReviews = 0;
            int savedReviews = 0;
            int duplicateReviews = 0;
            int filteredReviews = 0;
            int totalFailed = 0;
            
            // 2. 각 매장별로 리뷰 조회 및 저장
            for (StorePlatforms store : authorizedStores) {
                try {
                    log.info("매장 리뷰 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출 (쿠팡이츠는 body 없음)
                    HyphenReviewRespDto apiResponse = callHyphenReviewApiCoupang(
                            store.getLoginId(), store.getLoginPassword(), dateFrom, dateTo);
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 리뷰 데이터 처리
                        Map<String, Integer> storeResult = processStoreReviews(store, apiResponse, "쿠팡이츠");
                        totalReviews += storeResult.get("total");
                        savedReviews += storeResult.get("saved");
                        duplicateReviews += storeResult.get("duplicate");
                        filteredReviews += storeResult.get("filtered");
                        
                        // 동기화 시간 업데이트
                        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                        
                        log.info("쿠팡이츠 매장 리뷰 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                store.getStorePlatformCode(), storeResult.get("saved"));
                                
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 리뷰 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                
                // 테스트용: 매장별 지연 처리
                if (delayEnabled && authorizedStores.indexOf(store) < authorizedStores.size() - 1) {
                    try {
                        log.info("🕒 테스트용 지연 시간 - {}초 대기 (매장: {})", delaySeconds, store.getPlatformStoreName());
                        Thread.sleep(delaySeconds * 1000L);
                        log.info("⏰ 테스트용 지연 완료 - 다음 매장 처리 시작");
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("지연 처리 중단됨");
                    }
                }
            }
            
            // 결과 생성
            Map<String, Object> result = new HashMap<>();
            result.put("hqCode", hqCode);
            result.put("platform", "쿠팡이츠");
            result.put("authorizedStoreCount", authorizedStores.size());
            result.put("totalReviewsReceived", totalReviews);
            result.put("savedReviewCount", savedReviews);
            result.put("duplicateReviewCount", duplicateReviews);
            result.put("filteredReviewCount", filteredReviews);
            result.put("failedStoreCount", totalFailed);
            result.put("description", "하이픈 API 연동 - 쿠팡이츠 리뷰 - 본사: " + hqCode);
            
            String resultMessage = String.format("쿠팡이츠 리뷰 동기화 완료 - 총:%d건, 저장:%d건, 중복:%d건, 필터링:%d건, 실패:%d매장", 
                    totalReviews, savedReviews, duplicateReviews, filteredReviews, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, result);
            
        } catch (Exception e) {
            log.error("쿠팡이츠 리뷰 동기화 중 오류 발생", e);
            return RespDto.fail("쿠팡이츠 리뷰 동기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 전체 플랫폼 리뷰 동기화
     */
    public RespDto<Map<String, Object>> syncAllReviews(Integer hqCode) {
        try {
            log.info("전체 플랫폼 리뷰 동기화 시작 - 본사: {}", hqCode);
            
            Map<String, Object> allResults = new HashMap<>();
            
            // 1. 배민 리뷰 동기화
            RespDto<Map<String, Object>> baeminResult = syncBaeminReviews(hqCode);
            allResults.put("baemin", baeminResult);
            
            // 지연 시간 추가 (API 부하 방지)
            Thread.sleep(1000);
            
            // 2. 요기요 리뷰 동기화
            RespDto<Map<String, Object>> yogiyoResult = syncYogiyoReviews(hqCode);
            allResults.put("yogiyo", yogiyoResult);
            
            // 지연 시간 추가
            Thread.sleep(1000);
            
            // 3. 쿠팡이츠 리뷰 동기화
            RespDto<Map<String, Object>> coupangResult = syncCoupangReviews(hqCode);
            allResults.put("coupang", coupangResult);
            
            // 전체 결과 요약
            Map<String, Object> summary = new HashMap<>();
            summary.put("hqCode", hqCode);
            summary.put("totalPlatforms", 3);
            summary.put("successCount", 
                    (baeminResult.getCode() == 1 ? 1 : 0) +
                    (yogiyoResult.getCode() == 1 ? 1 : 0) +
                    (coupangResult.getCode() == 1 ? 1 : 0));
            summary.put("results", allResults);
            summary.put("description", "하이픈 API 연동 - 전체 플랫폼 리뷰");
            
            log.info("전체 플랫폼 리뷰 동기화 완료 - 본사: {}", hqCode);
            return RespDto.success("전체 플랫폼 리뷰 동기화 완료", summary);
            
        } catch (Exception e) {
            log.error("전체 플랫폼 리뷰 동기화 중 오류 발생", e);
            return RespDto.fail("전체 플랫폼 리뷰 동기화 실패: " + e.getMessage());
        }
    }
    
    /**
     * 하이픈 API 호출 - 배민 (body 없음)
     */
    private HyphenReviewRespDto callHyphenReviewApiBaemin(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenReviewApiByPlatform("배민", HYPHEN_API_URL_BAEMIN, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 - 요기요 (4개 파라미터)
     */
    private HyphenReviewRespDto callHyphenReviewApiYogiyo(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenReviewApiByPlatform("요기요", HYPHEN_API_URL_YOGIYO, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 - 쿠팡이츠 (body 없음)
     */
    private HyphenReviewRespDto callHyphenReviewApiCoupang(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenReviewApiByPlatform("쿠팡이츠", HYPHEN_API_URL_COUPANG, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 (공통 메서드)
     */
    private HyphenReviewRespDto callHyphenReviewApiByPlatform(String platform, String apiUrl, String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        
        // 플랫폼별 요청 바디 설정
        String requestBody = "";
        HttpRequest.BodyPublisher bodyPublisher;
        
//        if ("요기요".equals(platform)) {
//            // 요기요 요청 바디 (4개 파라미터)
//            requestBody = String.format(
//                "{\"userId\":\"%s\",\"userPw\":\"%s\",\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}", 
//                userId, userPw, dateFrom, dateTo
//            );
//            bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
//        } else {
//            // 배민, 쿠팡이츠는 body 없음
//            bodyPublisher = HttpRequest.BodyPublishers.noBody();
//        }
        
        requestBody = String.format(
                "{\"userId\":\"%s\",\"userPw\":\"%s\",\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}", 
                userId, userPw, dateFrom, dateTo
            );
            bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
            
        log.info("=== 하이픈 리뷰 API 요청 ({}) ===", platform);
        log.info("URL: {}", apiUrl);
        log.info("Hkey: {}", HKEY);
        log.info("User-Id: {}", USER_ID);
        log.info("Request Body: {}", requestBody);
        
        // HTTP 요청 생성
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Hkey", HKEY)
                .header("hyphen-gustation", "Y")
                .header("user-id", USER_ID);
        
        HttpRequest request = requestBuilder.POST(bodyPublisher).build();
        
        // HTTP 요청 실행
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        
        log.info("=== 하이픈 리뷰 API 응답 ({}) ===", platform);
        log.info("Status Code: {}", response.statusCode());
        log.info("Response Body: {}", response.body());
        log.info("========================");
        
        if (response.statusCode() == 200) {
            try {
                HyphenReviewRespDto result = objectMapper.readValue(response.body(), HyphenReviewRespDto.class);
                
                // 하이픈 API 에러 체크
                if (result.getCommon() != null && "Y".equals(result.getCommon().getErrYn())) {
                    String errorMsg = result.getCommon().getErrMsg();
                    log.error("하이픈 리뷰 API 에러 ({}) - 에러코드: {}, 에러메시지: {}", 
                            platform, result.getCommon().getErrCd(), errorMsg);
                    
                    throw new RuntimeException("하이픈 리뷰 API 에러: " + errorMsg);
                }
                
                return result;
            } catch (Exception e) {
                log.error("JSON 파싱 오류 ({}) - 원본 응답: {}", platform, response.body(), e);
                throw new RuntimeException("JSON 파싱 실패: " + e.getMessage() + "\n원본 응답: " + response.body());
            }
        } else {
            log.error("하이픈 리뷰 API HTTP 에러 ({}) - Status: {}, Body: {}", platform, response.statusCode(), response.body());
            throw new RuntimeException("하이픈 리뷰 API 호출 실패 - Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
    
    /**
     * 매장별 리뷰 데이터 처리
     */
    @Transactional
    private Map<String, Integer> processStoreReviews(StorePlatforms store, HyphenReviewRespDto apiResponse, String platform) {
        
        Map<String, Integer> result = new HashMap<>();
        result.put("total", 0);
        result.put("saved", 0);
        result.put("duplicate", 0);
        result.put("filtered", 0);
        
        try {
            if (apiResponse.getData() == null || apiResponse.getData().getStoreList() == null) {
                log.warn("리뷰 데이터가 없습니다 - 매장: {}", store.getPlatformStoreName());
                return result;
            }
            
            // 매장별 리뷰 처리
            for (HyphenReviewRespDto.StoreData storeData : apiResponse.getData().getStoreList()) {
                String apiStoreId = storeData.getStoreId();
                String platformStoreId = store.getPlatformStoreId();
                
                // storeId 검증
                if (apiStoreId == null || !apiStoreId.equals(platformStoreId)) {
                    log.debug("권한 없는 매장 리뷰 필터링 - API storeId: {}, 매장 storeId: {}", 
                            apiStoreId, platformStoreId);
                    if (storeData.getReviewList() != null) {
                        result.put("filtered", result.get("filtered") + storeData.getReviewList().size());
                    }
                    continue;
                }
                
                // 리뷰 저장
                if (storeData.getReviewList() != null) {
                    for (HyphenReviewRespDto.ReviewData review : storeData.getReviewList()) {
                        result.put("total", result.get("total") + 1);
                        
                        // 중복 검사
                        String orderReviewId = review.getOrderReviewId();
                        if (orderReviewId != null && 
                            reviewPlatformRepository.countByPlatformAndOrderReviewId(platform, orderReviewId) > 0) {
                            result.put("duplicate", result.get("duplicate") + 1);
                            log.debug("중복 리뷰 스킵 - platform: {}, orderReviewId: {}", platform, orderReviewId);
                            continue;
                        }
                        
                        // DB 저장
                        saveReviewToDatabase(review, store, platform);
                        result.put("saved", result.get("saved") + 1);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("매장 리뷰 데이터 처리 중 오류 발생 - 매장: {}", store.getPlatformStoreName(), e);
            // 예외 재발생하지 말고 로그만 남김
        }
        
        return result;
    }
    
    /**
     * 개별 리뷰를 DB에 저장
     */
    @Transactional
    private void saveReviewToDatabase(HyphenReviewRespDto.ReviewData review, StorePlatforms store, String platform) {
        try {
            // 1. review_platform 테이블에 INSERT
            
            // 날짜 변환
            LocalDate reviewDate = parseReviewDate(review.getReviewDt());
            LocalTime reviewTime = parseReviewTime(review.getReviewTm());
            
            // 별점 변환
            Integer rating = parseRating(review.getAllStar());
            
            // 텍스트 길이 제한
            String orderMenu = limitString(review.getJumun(), 250);
            String content = limitString(review.getComment(), 500);
            String ownerReplyContent = limitString(review.getOwnerReply(), 500);
            
            // 사장댓글 날짜
            LocalDate ownerReplyDate = parseOwnerReplyDate(review.getOwnerReplyDt());
            LocalTime ownerReplyTime = parseReviewTime(review.getOwnerReplyTm());
            
            // 이미지 여부
            char hasImages = (review.getReviewImgList() != null && !review.getReviewImgList().isEmpty()) ? 'Y' : 'N';
            
            // 원본 데이터 (개별 리뷰만)
            String rawData = objectMapper.writeValueAsString(review);
            
            // DB INSERT
            reviewPlatformRepository.insertReview(
                    store.getStorePlatformCode(),
                    store.getCustomerCode(),
                    store.getBrandCode(),
                    platform,
                    reviewDate != null ? reviewDate.toString() : null,
                    reviewTime != null ? reviewTime.toString() : null,
                    rating,
                    orderMenu,
                    content,
                    ownerReplyContent,
                    ownerReplyDate != null ? ownerReplyDate.toString() : null,
                    ownerReplyTime != null ? ownerReplyTime.toString() : null,
                    String.valueOf(hasImages),
                    rawData,
                    "하이픈 API 연동 - 리뷰 - 본사:" + store.getHqCode()
            );
            
            // 2. 이미지가 있으면 review_image_platform 테이블에 INSERT
            if (review.getReviewImgList() != null && !review.getReviewImgList().isEmpty()) {
                Integer reviewPlatformCode = reviewPlatformRepository.getLastInsertId();
                
                for (int i = 0; i < review.getReviewImgList().size(); i++) {
                    HyphenReviewRespDto.ReviewImage image = review.getReviewImgList().get(i);
                    
                    reviewPlatformRepository.insertReviewImage(
                            reviewPlatformCode,
                            store.getStorePlatformCode(),
                            store.getCustomerCode(),
                            store.getBrandCode(),
                            i + 1, // 순서 (1부터 시작)
                            image.getReviewImg(),
                            "리뷰 이미지"
                    );
                }
                
                log.debug("리뷰 이미지 저장 완료 - 리뷰ID: {}, 이미지수: {}", 
                        reviewPlatformCode, review.getReviewImgList().size());
            }
            
        } catch (Exception e) {
            log.error("리뷰 DB 저장 중 오류 발생 - orderReviewId: {}, 매장: {}", 
                    review.getOrderReviewId(), store.getPlatformStoreName(), e);
            // 개별 리뷰 저장 실패는 전체를 망치지 않도록 예외를 다시 던지지 않음
        }
    }
    
    /**
     * 날짜 파싱 (YYYYMMDD → LocalDate)
     */
    private LocalDate parseReviewDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            // 요기요는 reviewDt가 빈 문자열로 올 수 있음 - 현재 날짜 사용
            log.debug("리뷰 날짜가 없어서 현재 날짜 사용: {}", dateStr);
            return LocalDate.now();
        }
        
        if (dateStr.length() != 8) {
            log.warn("날짜 형식 오류 (8자리 아님): {} - 현재 날짜 사용", dateStr);
            return LocalDate.now();
        }
        
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {} - 현재 날짜 사용", dateStr);
            return LocalDate.now();
        }
    }
    
    /**
     * 날짜 파싱 (YYYYMMDD → LocalDate) - 사장댓글 날짜용 (선택사항, 없으면 null)
     */
    private LocalDate parseOwnerReplyDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            // 사장댓글 날짜는 없으면 null 처리
            return null;
        }
        
        if (dateStr.length() != 8) {
            log.debug("사장댓글 날짜 형식 오류: {} - null 처리", dateStr);
            return null;
        }
        
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (DateTimeParseException e) {
            log.debug("사장댓글 날짜 파싱 실패: {} - null 처리", dateStr);
            return null;
        }
    }
    
    /**
     * 시간 파싱 (HHMMSS → LocalTime)
     */
    private LocalTime parseReviewTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty() || timeStr.length() != 6) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HHmmss"));
        } catch (DateTimeParseException e) {
            log.warn("시간 파싱 실패: {}", timeStr);
            return null;
        }
    }
    
    /**
     * 별점 파싱 (String → Integer)
     */
    private Integer parseRating(String ratingStr) {
        if (ratingStr == null || ratingStr.trim().isEmpty()) {
            return 0;
        }
        
        try {
            // 소수점이 있으면 반올림
            double rating = Double.parseDouble(ratingStr);
            return (int) Math.round(rating);
        } catch (NumberFormatException e) {
            log.warn("별점 파싱 실패: {}", ratingStr);
            return 0;
        }
    }
    
    /**
     * 문자열 길이 제한
     */
    private String limitString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        
        if (str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength);
    }
}