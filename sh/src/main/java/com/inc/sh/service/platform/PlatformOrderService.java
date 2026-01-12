package com.inc.sh.service.platform;

import com.inc.sh.dto.platform.reqDto.HyphenOrderReqDto;
import com.inc.sh.dto.platform.respDto.HyphenOrderRespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import com.inc.sh.common.dto.RespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformOrderService {
    
    private final StorePlatformsRepository storePlatformsRepository;
    private final OrderPlatformsRepository orderPlatformsRepository;
    private final OrderDetailPlatformRepository orderDetailPlatformRepository;
    private final OrderOptionPlatformRepository orderOptionPlatformRepository;
    
    private final ObjectMapper objectMapper;
    
    // 테스트용 지연 설정 (운영시 false로 변경)
    @Value("${platform.sync.delay.enabled:true}")
    private boolean delayEnabled;
    
    @Value("${platform.sync.delay.seconds:120}")
    private int delaySeconds;
    
    // 하이픈 API 설정
    private static final String HYPHEN_API_URL_BAEMIN = "https://api.hyphen.im/in0022000083";
    private static final String HYPHEN_API_URL_YOGIYO = "https://api.hyphen.im/in0023000085";  // 요기요
    private static final String HYPHEN_API_URL_COUPANG = "https://api.hyphen.im/in0024000086"; // 쿠팡이츠

    private static final String HKEY = "30cab2cddc0a9352";
    private static final String USER_ID = "shcompany2";
    
    /**
     * 배민 주문내역 전체 동기화
     */
    @Transactional
    public RespDto<String> syncBaeminOrders(Integer hqCode) {
        try {
            log.info("배민 주문내역 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 배민 매장 정보 조회 (본사코드 조건 추가)
            List<StorePlatforms> baeminStores = storePlatformsRepository.findByPlatformAndHqCodeAndIsActiveTrue("배민", hqCode);
            
            if (baeminStores.isEmpty()) {
                log.warn("배민 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("배민 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            int totalSuccess = 0;
            int totalFailed = 0;
            
            // 2. 조회기간 설정 (오늘부터 7일전까지) - YYYYMMDD 형식
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(7);
            String dateFrom = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));  // YYYYMMDD 형식
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));      // YYYYMMDD 형식
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            // 3. 각 매장별로 주문내역 조회
            for (StorePlatforms store : baeminStores) {
                try {
                    log.info("매장 주문내역 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출
                    HyphenOrderRespDto apiResponse = callHyphenOrderApi(
                            store.getLoginId(), 
                            store.getLoginPassword(), 
                            dateFrom, 
                            dateTo
                    );
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 성공 응답 처리
                        if (apiResponse.getData() != null && apiResponse.getData().getTouchOrderList() != null) {
                            // DB 저장
                            int savedCount = saveOrdersToDatabase(store, apiResponse.getData().getTouchOrderList());
                            totalSuccess += savedCount;
                            
                            // 동기화 시간 업데이트
                            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                            
                            log.info("매장 주문내역 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                    store.getStorePlatformCode(), savedCount);
                        } else {
                            log.warn("매장 주문내역 조회 결과 없음 - 매장코드: {}", store.getStorePlatformCode());
                        }
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 주문내역 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                // 테스트용: 매장별 지연 처리 (마지막 매장 제외)
                if (delayEnabled && baeminStores.indexOf(store) < baeminStores.size() - 1) {
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
            
            String resultMessage = String.format("배민 주문내역 동기화 완료 - 성공: %d건, 실패: %d건", 
                    totalSuccess, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, resultMessage);
            
        } catch (Exception e) {
            log.error("배민 주문내역 동기화 중 오류 발생", e);
            return RespDto.fail("배민 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 요기요 주문내역 전체 동기화
     */
    @Transactional
    public RespDto<String> syncYogiyoOrders(Integer hqCode) {
        try {
            log.info("요기요 주문내역 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 요기요 매장 정보 조회 (본사코드 조건 추가)
            List<StorePlatforms> yogiyoStores = storePlatformsRepository.findByPlatformAndHqCodeAndIsActiveTrue("요기요", hqCode);
            
            if (yogiyoStores.isEmpty()) {
                log.warn("요기요 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("요기요 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            int totalSuccess = 0;
            int totalFailed = 0;
            
            // 2. 조회기간 설정 (오늘부터 7일전까지) - YYYYMMDD 형식
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(20);
            String dateFrom = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));  // YYYYMMDD 형식
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));      // YYYYMMDD 형식
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            // 3. 각 매장별로 주문내역 조회
            for (StorePlatforms store : yogiyoStores) {
                try {
                    log.info("매장 주문내역 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출 (요기요용)
                    HyphenOrderRespDto apiResponse = callHyphenOrderApiYogiyo(
                            store.getLoginId(), 
                            store.getLoginPassword(), 
                            dateFrom, 
                            dateTo
                    );
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 성공 응답 처리 - DB 저장 추가
                        if (apiResponse.getData() != null && apiResponse.getData().getTouchOrderList() != null) {
                            // DB 저장
                            int savedCount = saveOrdersToDatabase(store, apiResponse.getData().getTouchOrderList());
                            totalSuccess += savedCount;
                            
                            // 동기화 시간 업데이트
                            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                            
                            log.info("요기요 주문내역 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                    store.getStorePlatformCode(), savedCount);
                        } else {
                            log.warn("요기요 매장 주문내역 조회 결과 없음 - 매장코드: {}", store.getStorePlatformCode());
                        }
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 주문내역 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                // 테스트용: 매장별 지연 처리 (마지막 매장 제외)
                if (delayEnabled && yogiyoStores.indexOf(store) < yogiyoStores.size() - 1) {
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
            
            String resultMessage = String.format("요기요 주문내역 동기화 완료 - 성공: %d건, 실패: %d건", 
                    totalSuccess, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, resultMessage);
            
        } catch (Exception e) {
            log.error("요기요 주문내역 동기화 중 오류 발생", e);
            return RespDto.fail("요기요 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 쿠팡이츠 주문내역 전체 동기화
     */
    @Transactional
    public RespDto<String> syncCoupangOrders(Integer hqCode) {
        try {
            log.info("쿠팡이츠 주문내역 동기화 시작 - 본사코드: {}", hqCode);
            
            // 1. 쿠팡이츠 매장 정보 조회 (본사코드 조건 추가)
            List<StorePlatforms> coupangStores = storePlatformsRepository.findByPlatformAndHqCodeAndIsActiveTrue("쿠팡이츠", hqCode);
            
            if (coupangStores.isEmpty()) {
                log.warn("쿠팡이츠 매장 정보가 없습니다. - 본사코드: {}", hqCode);
                return RespDto.fail("쿠팡이츠 매장 정보가 없습니다. (본사코드: " + hqCode + ")");
            }
            
            int totalSuccess = 0;
            int totalFailed = 0;
            
            // 2. 조회기간 설정 (오늘부터 7일전까지) - YYYYMMDD 형식
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(20);
            String dateFrom = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));  // YYYYMMDD 형식
            String dateTo = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));      // YYYYMMDD 형식
            
            log.info("조회기간: {} ~ {}", dateFrom, dateTo);
            
            // 3. 각 매장별로 주문내역 조회
            for (StorePlatforms store : coupangStores) {
                try {
                    log.info("매장 주문내역 조회 시작 - 매장코드: {}, 매장명: {}", 
                            store.getStorePlatformCode(), store.getPlatformStoreName());
                    
                    // 하이픈 API 호출 (쿠팡이츠용)
                    HyphenOrderRespDto apiResponse = callHyphenOrderApiCoupang(
                            store.getLoginId(), 
                            store.getLoginPassword(), 
                            dateFrom, 
                            dateTo
                    );
                    
                    if (apiResponse != null && apiResponse.getCommon() != null) {
                        // 에러 체크
                        if ("Y".equals(apiResponse.getCommon().getErrYn())) {
                            log.error("하이픈 API 에러 - 매장코드: {}, 에러메시지: {}", 
                                    store.getStorePlatformCode(), apiResponse.getCommon().getErrMsg());
                            totalFailed++;
                            continue;
                        }
                        
                        // 성공 응답 처리 - DB 저장 추가
                        if (apiResponse.getData() != null && apiResponse.getData().getTouchOrderList() != null) {
                            // DB 저장
                            int savedCount = saveOrdersToDatabase(store, apiResponse.getData().getTouchOrderList());
                            totalSuccess += savedCount;
                            
                            // 동기화 시간 업데이트
                            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                            storePlatformsRepository.updateLastSyncedAt(store.getStorePlatformCode(), currentTime);
                            
                            log.info("쿠팡이츠 주문내역 동기화 완료 - 매장코드: {}, 저장건수: {}", 
                                    store.getStorePlatformCode(), savedCount);
                        } else {
                            log.warn("쿠팡이츠 매장 주문내역 조회 결과 없음 - 매장코드: {}", store.getStorePlatformCode());
                        }
                    } else {
                        log.warn("하이픈 API 응답이 null - 매장코드: {}", store.getStorePlatformCode());
                        totalFailed++;
                    }
                    
                } catch (Exception e) {
                    log.error("매장 주문내역 동기화 실패 - 매장코드: {}", store.getStorePlatformCode(), e);
                    totalFailed++;
                }
                // 테스트용: 매장별 지연 처리 (마지막 매장 제외)
                if (delayEnabled && coupangStores.indexOf(store) < coupangStores.size() - 1) {
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
            
            String resultMessage = String.format("쿠팡이츠 주문내역 동기화 완료 - 성공: %d건, 실패: %d건", 
                    totalSuccess, totalFailed);
            log.info(resultMessage);
            
            return RespDto.success(resultMessage, resultMessage);
            
        } catch (Exception e) {
            log.error("쿠팡이츠 주문내역 동기화 중 오류 발생", e);
            return RespDto.fail("쿠팡이츠 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 하이픈 API 호출 (배민)
     */
    private HyphenOrderRespDto callHyphenOrderApi(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenOrderApiByPlatform("배민", HYPHEN_API_URL_BAEMIN, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 (요기요)
     */
    private HyphenOrderRespDto callHyphenOrderApiYogiyo(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenOrderApiByPlatform("요기요", HYPHEN_API_URL_YOGIYO, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 (쿠팡이츠)
     */
    private HyphenOrderRespDto callHyphenOrderApiCoupang(String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        return callHyphenOrderApiByPlatform("쿠팡이츠", HYPHEN_API_URL_COUPANG, userId, userPw, dateFrom, dateTo);
    }
    
    /**
     * 하이픈 API 호출 (공통 메서드)
     */
    private HyphenOrderRespDto callHyphenOrderApiByPlatform(String platform, String apiUrl, String userId, String userPw, String dateFrom, String dateTo) throws Exception {
        
        // 플랫폼별 요청 바디 설정
        String requestBody;
        HttpRequest.BodyPublisher bodyPublisher;
        
        if ("쿠팡이츠".equals(platform)) {
            // 쿠팡이츠 요청 바디 (4개 파라미터) - body 없음이 아니라 4개 파라미터
            requestBody = String.format(
                "{\"userId\":\"%s\",\"userPw\":\"%s\",\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}", 
                userId, userPw, dateFrom, dateTo
            );
            bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
        } else if ("요기요".equals(platform)) {
            // 요기요 요청 바디 (4개 파라미터)
            requestBody = String.format(
                "{\"userId\":\"%s\",\"userPw\":\"%s\",\"dateFrom\":\"%s\",\"dateTo\":\"%s\"}", 
                userId, userPw, dateFrom, dateTo
            );
            bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
        } else {
            // 배민 요청 바디 (6개 파라미터)
            HyphenOrderReqDto reqDto = HyphenOrderReqDto.builder()
                    .userId(userId)
                    .userPw(userPw)
                    .dateFrom(dateFrom)
                    .dateTo(dateTo)
                    .processYn("Y")
                    .detailYn("Y")
                    .build();
            requestBody = objectMapper.writeValueAsString(reqDto);
            bodyPublisher = HttpRequest.BodyPublishers.ofString(requestBody);
        }
        
        log.info("=== 하이픈 API 요청 ({}) ===", platform);
        log.info("URL: {}", apiUrl);
        log.info("Hkey: {}", HKEY);
        log.info("User-Id: {}", USER_ID);
        log.info("Request Body: {}", requestBody);
        
        // HTTP 요청 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Hkey", HKEY)
                .header("Hyphen-Gustation", "Y")
                .header("User-Id", USER_ID)
                .POST(bodyPublisher)
                .build();
        
        // HTTP 요청 실행
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        
        log.info("=== 하이픈 API 응답 ({}) ===", platform);
        log.info("Status Code: {}", response.statusCode());
        log.info("Response Body: {}", response.body());
        log.info("========================");
        
        if (response.statusCode() == 200) {
            try {
                HyphenOrderRespDto result = objectMapper.readValue(response.body(), HyphenOrderRespDto.class);
                
                // 하이픈 API 에러 체크 (errYn: "Y")
                if (result.getCommon() != null && "Y".equals(result.getCommon().getErrYn())) {
                    String errorMsg = result.getCommon().getErrMsg();
                    log.error("하이픈 API 에러 ({}) - 에러코드: {}, 에러메시지: {}", 
                            platform, result.getCommon().getErrCd(), errorMsg);
                    
                    // 120초 제한 에러나 기타 API 에러는 예외 발생
                    throw new RuntimeException("하이픈 API 에러: " + errorMsg);
                }
                
                return result;
            } catch (Exception e) {
                log.error("JSON 파싱 오류 ({}) - 원본 응답: {}", platform, response.body(), e);
                throw new RuntimeException("JSON 파싱 실패: " + e.getMessage() + "\n원본 응답: " + response.body());
            }
        } else {
            // 400, 500 등 HTTP 에러 처리
            log.error("하이픈 API HTTP 에러 ({}) - Status: {}, Body: {}", platform, response.statusCode(), response.body());
            throw new RuntimeException("하이픈 API 호출 실패 - Status: " + response.statusCode() + ", Body: " + response.body());
        }
    }
    
    /**
     * 주문 데이터를 DB에 저장 - 플랫폼별 핵심 필드 매핑 + storeId 검증
     */
    private int saveOrdersToDatabase(StorePlatforms store, List<HyphenOrderRespDto.OrderData> orderDataList) {
        int savedCount = 0;
        int filteredCount = 0;
        
        log.info("주문 데이터 저장 시작 - 매장코드: {}, 플랫폼: {}, 주문건수: {}", 
                store.getStorePlatformCode(), store.getPlatform(), orderDataList.size());
        
        for (HyphenOrderRespDto.OrderData orderData : orderDataList) {
            try {
                // storeId 검증: API 응답의 storeId와 매장의 platformStoreId 일치 확인
                String apiStoreId = orderData.getStoreId();
                String platformStoreId = store.getPlatformStoreId();
                
                if (apiStoreId == null || !apiStoreId.equals(platformStoreId)) {
                    log.debug("권한 없는 매장의 주문 필터링 - API storeId: {}, 매장 storeId: {}, 주문번호: {}", 
                            apiStoreId, platformStoreId, orderData.getOrderNo());
                    filteredCount++;
                    continue; // 다른 매장의 주문은 저장하지 않음
                }
                
                // 플랫폼별 데이터 매핑
                String orderNo = extractOrderNo(orderData, store.getPlatform());
                String orderDate = extractOrderDate(orderData, store.getPlatform());
                String orderName = extractOrderName(orderData, store.getPlatform());
                Integer orderAmount = extractOrderAmount(orderData, store.getPlatform());
                
                log.debug("권한 확인된 주문 처리 중 - 주문번호: {}, 플랫폼: {}, 매장ID: {}", 
                        orderNo, store.getPlatform(), apiStoreId);
                
                // 중복 체크
                if (orderPlatformsRepository.existsByOrderNo(orderNo)) {
                    log.debug("이미 존재하는 주문 스킵 - 주문번호: {}", orderNo);
                    continue;
                }
                
                // OrderPlatforms 생성 (핵심 4개 필드)
                OrderPlatforms orderPlatform = new OrderPlatforms();
                orderPlatform.setStorePlatformCode(store.getStorePlatformCode());
                orderPlatform.setCustomerCode(store.getCustomerCode());
                orderPlatform.setBrandCode(store.getBrandCode());
                orderPlatform.setPlatform(store.getPlatform());
                orderPlatform.setOrderNo(orderNo);
                orderPlatform.setOrderDate(orderDate != null ? orderDate : "");
                orderPlatform.setOrderName(orderName); // 요기요는 null
                orderPlatform.setOrderAmount(orderAmount != null ? orderAmount : 0);
                
                // 나머지 필드는 기본값
                orderPlatform.setOrderTime(null);
                orderPlatform.setOrderDivision(null);
                orderPlatform.setDeliveryType(null);
                orderPlatform.setPaymentMethod(null);
                orderPlatform.setDeliveryAmount(0);
                orderPlatform.setDiscountAmount(0);
                orderPlatform.setCouponAmount(0);
                orderPlatform.setOrderFee(0);
                orderPlatform.setCardFee(0);
                orderPlatform.setTax(0);
                orderPlatform.setSettleDate(null);
                orderPlatform.setSettleAmount(0);
                orderPlatform.setOfflineOrderAmount(0);
                orderPlatform.setDescription("하이픈 API 연동 - 본사:" + store.getHqCode());
                
                log.debug("DB 저장 시도 - 주문번호: {}, 매장코드: {}, 금액: {}", 
                        orderNo, store.getStorePlatformCode(), orderAmount);
                
                // DB 저장
                orderPlatform = orderPlatformsRepository.save(orderPlatform);
                
                log.debug("DB 저장 성공 - 주문번호: {}, 생성된 ID: {}", 
                        orderNo, orderPlatform.getOrderPlatformCode());
                
                savedCount++;
                
            } catch (Exception e) {
                log.error("주문 저장 실패 - 플랫폼: {}, 에러: {}", store.getPlatform(), e.getMessage());
                log.error("상세 에러:", e);
            }
        }
        
        log.info("주문 데이터 저장 완료 - 매장코드: {}, 플랫폼: {}, 총주문: {}건, 권한확인: {}건, 필터링: {}건", 
                store.getStorePlatformCode(), store.getPlatform(), orderDataList.size(), savedCount, filteredCount);
        
        return savedCount;
    }
    
    /**
     * 플랫폼별 주문번호 추출
     */
    private String extractOrderNo(HyphenOrderRespDto.OrderData orderData, String platform) {
        return orderData.getOrderNo(); // 모든 플랫폼 공통
    }
    
    /**
     * 플랫폼별 주문날짜 추출
     */
    private String extractOrderDate(HyphenOrderRespDto.OrderData orderData, String platform) {
        return orderData.getOrderDate(); // 모든 플랫폼에서 orderDt 사용
    }
    
    /**
     * 플랫폼별 주문명 추출
     */
    private String extractOrderName(HyphenOrderRespDto.OrderData orderData, String platform) {
        if ("요기요".equals(platform)) {
            return null; // 요기요는 orderName 필드 없음
        }
        return orderData.getOrderName(); // 배민, 쿠팡이츠
    }
    
    /**
     * 플랫폼별 주문금액 추출
     */
    private Integer extractOrderAmount(HyphenOrderRespDto.OrderData orderData, String platform) {
        if ("쿠팡이츠".equals(platform)) {
            return orderData.getSettleAmount(); // 쿠팡이츠는 settleAmt 사용
        }
        return orderData.getOrderAmount(); // 배민, 요기요는 orderAmt 사용
    }
}