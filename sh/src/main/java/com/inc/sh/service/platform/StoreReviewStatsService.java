package com.inc.sh.service.platform;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.StoreReviewStatsReqDto;
import com.inc.sh.dto.platform.reqDto.PeriodReviewReqDto;
import com.inc.sh.dto.platform.respDto.StoreReviewStatsRespDto;
import com.inc.sh.dto.platform.respDto.PeriodReviewRespDto;
import com.inc.sh.repository.StoreReviewStatsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class StoreReviewStatsService {
    
    private final StoreReviewStatsRepository storeReviewStatsRepository;
    
    /**
     * 매장별 리뷰현황 조회
     */
    public RespDto<StoreReviewStatsRespDto> getStoreReviewStats(StoreReviewStatsReqDto reqDto) {
        try {
            log.info("매장별 리뷰현황 조회 시작 - 거래처: {}, 브랜드: {}, 기간: {}~{}", 
                    reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getStartYearMonth(), reqDto.getEndYearMonth());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            if (reqDto.getBrandCode() == null) {
                return RespDto.fail("브랜드코드는 필수입니다.");
            }
            if (reqDto.getStartYearMonth() == null || reqDto.getEndYearMonth() == null) {
                return RespDto.fail("시작년월과 종료년월은 필수입니다.");
            }
            if (!isValidYearMonth(reqDto.getStartYearMonth()) || !isValidYearMonth(reqDto.getEndYearMonth())) {
                return RespDto.fail("년월 형식이 잘못되었습니다. YYYYMM 형식으로 입력해주세요.");
            }
            if (reqDto.getStartYearMonth().compareTo(reqDto.getEndYearMonth()) > 0) {
                return RespDto.fail("시작년월이 종료년월보다 클 수 없습니다.");
            }
            
            // 2. 매장별 플랫폼별 리뷰 통계 조회
            List<Map<String, Object>> rawStoreStats = storeReviewStatsRepository
                    .findStoreReviewStatsByPeriod(reqDto.getHqCode(), reqDto.getBrandCode(), 
                                                  reqDto.getStartYearMonth(), reqDto.getEndYearMonth());
            
            // 3. 전체 합계 통계 조회
            List<Map<String, Object>> totalStats = storeReviewStatsRepository
                    .findTotalReviewStatsByPeriod(reqDto.getHqCode(), reqDto.getBrandCode(), 
                                                  reqDto.getStartYearMonth(), reqDto.getEndYearMonth());
            
            // 4. 매장 목록 조회 (리뷰가 없는 매장도 포함)
            List<Map<String, Object>> allStores = storeReviewStatsRepository
                    .findStoresByHqCode(reqDto.getHqCode(), reqDto.getBrandCode());
            
            // 5. 데이터 가공
            List<StoreReviewStatsRespDto.StoreReviewStat> resultList = processStoreReviewStats(
                    rawStoreStats, totalStats, allStores);
            
            StoreReviewStatsRespDto respDto = StoreReviewStatsRespDto.builder()
                    .storeReviewStats(resultList)
                    .build();
            
            String brandInfo = reqDto.getBrandCode() == 0 ? "전체" : "브랜드(" + reqDto.getBrandCode() + ")";
            log.info("매장별 리뷰현황 조회 완료 - 거래처: {}, {}, 매장수: {}", 
                    reqDto.getHqCode(), brandInfo, resultList.size());
            
            return RespDto.success("매장별 리뷰현황 조회 완료", respDto);
            
        } catch (Exception e) {
            log.error("매장별 리뷰현황 조회 중 오류 발생", e);
            return RespDto.fail("매장별 리뷰현황 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 매장별 리뷰 통계 데이터 가공
     */
    private List<StoreReviewStatsRespDto.StoreReviewStat> processStoreReviewStats(
            List<Map<String, Object>> rawStoreStats,
            List<Map<String, Object>> totalStats,
            List<Map<String, Object>> allStores) {
        
        List<StoreReviewStatsRespDto.StoreReviewStat> resultList = new ArrayList<>();
        
        // 1. 전체 합계 행 생성
        StoreReviewStatsRespDto.StoreReviewStat totalStat = createTotalStat(totalStats);
        resultList.add(totalStat);
        
        // 2. 매장별 통계를 Map으로 그룹핑
        Map<String, Map<String, Map<String, Object>>> storeStatsMap = groupStoreStatsByStore(rawStoreStats);
        
        // 3. 매장별 통계 생성
        int rowNumber = 1;
        for (Map<String, Object> storeInfo : allStores) {
            String storeName = (String) storeInfo.get("store_name");
            
            StoreReviewStatsRespDto.StoreReviewStat storeStat = createStoreStat(
                    rowNumber, storeName, storeStatsMap.get(storeName));
            resultList.add(storeStat);
            rowNumber++;
        }
        
        return resultList;
    }
    
    /**
     * 전체 합계 통계 생성
     */
    private StoreReviewStatsRespDto.StoreReviewStat createTotalStat(List<Map<String, Object>> totalStats) {
        
        Map<String, Map<String, Object>> platformStats = new HashMap<>();
        
        // 플랫폼별 통계를 Map으로 변환
        for (Map<String, Object> stat : totalStats) {
            String platform = (String) stat.get("platform");
            platformStats.put(platform, stat);
        }
        
        // 전체 합계 계산
        int totalReviewCount = 0;
        double totalRatingSum = 0.0;
        int platformCount = 0;
        
        // 배민, 요기요, 쿠팡이츠 각각의 통계 추출
        Map<String, Object> baeminStat = platformStats.get("배민");
        Map<String, Object> yogiyoStat = platformStats.get("요기요");
        Map<String, Object> coupangStat = platformStats.get("쿠팡이츠");
        
        // 전체 통계 계산
        if (baeminStat != null) {
            totalReviewCount += ((Number) baeminStat.get("review_count")).intValue();
            totalRatingSum += ((Number) baeminStat.get("avg_rating")).doubleValue() * 
                             ((Number) baeminStat.get("review_count")).intValue();
            platformCount++;
        }
        if (yogiyoStat != null) {
            totalReviewCount += ((Number) yogiyoStat.get("review_count")).intValue();
            totalRatingSum += ((Number) yogiyoStat.get("avg_rating")).doubleValue() * 
                             ((Number) yogiyoStat.get("review_count")).intValue();
            platformCount++;
        }
        if (coupangStat != null) {
            totalReviewCount += ((Number) coupangStat.get("review_count")).intValue();
            totalRatingSum += ((Number) coupangStat.get("avg_rating")).doubleValue() * 
                             ((Number) coupangStat.get("review_count")).intValue();
            platformCount++;
        }
        
        // 가중평균 계산
        Double totalAvgRating = totalReviewCount > 0 ? 
                Math.round((totalRatingSum / totalReviewCount) * 10.0) / 10.0 : 0.0;
        
        return StoreReviewStatsRespDto.StoreReviewStat.builder()
                .rowNumber(0)
                .storeName("합계")
                .totalReviewCount(totalReviewCount)
                .totalAvgRating(totalAvgRating)
                .baeminReviewCount(baeminStat != null ? ((Number) baeminStat.get("review_count")).intValue() : 0)
                .baeminAvgRating(baeminStat != null ? ((Number) baeminStat.get("avg_rating")).doubleValue() : 0.0)
                .yogiyoReviewCount(yogiyoStat != null ? ((Number) yogiyoStat.get("review_count")).intValue() : 0)
                .yogiyoAvgRating(yogiyoStat != null ? ((Number) yogiyoStat.get("avg_rating")).doubleValue() : 0.0)
                .coupangReviewCount(coupangStat != null ? ((Number) coupangStat.get("review_count")).intValue() : 0)
                .coupangAvgRating(coupangStat != null ? ((Number) coupangStat.get("avg_rating")).doubleValue() : 0.0)
                .storePlatformCode(null)
                .build();
    }
    
    /**
     * 매장별 통계를 매장명으로 그룹핑
     */
    private Map<String, Map<String, Map<String, Object>>> groupStoreStatsByStore(List<Map<String, Object>> rawStats) {
        
        Map<String, Map<String, Map<String, Object>>> result = new HashMap<>();
        
        for (Map<String, Object> stat : rawStats) {
            String storeName = (String) stat.get("store_name");
            String platform = (String) stat.get("platform");
            
            result.computeIfAbsent(storeName, k -> new HashMap<>()).put(platform, stat);
        }
        
        return result;
    }
    
    /**
     * 개별 매장 통계 생성
     */
    private StoreReviewStatsRespDto.StoreReviewStat createStoreStat(
            int rowNumber, String storeName, 
            Map<String, Map<String, Object>> platformStats) {
        
        if (platformStats == null) {
            platformStats = new HashMap<>();
        }
        
        // 플랫폼별 통계 추출
        Map<String, Object> baeminStat = platformStats.get("배민");
        Map<String, Object> yogiyoStat = platformStats.get("요기요");
        Map<String, Object> coupangStat = platformStats.get("쿠팡이츠");
        
        // 전체 합계 계산
        int totalReviewCount = 0;
        double totalRatingSum = 0.0;
        
        if (baeminStat != null) {
            totalReviewCount += ((Number) baeminStat.get("review_count")).intValue();
            totalRatingSum += ((Number) baeminStat.get("avg_rating")).doubleValue() * 
                             ((Number) baeminStat.get("review_count")).intValue();
        }
        if (yogiyoStat != null) {
            totalReviewCount += ((Number) yogiyoStat.get("review_count")).intValue();
            totalRatingSum += ((Number) yogiyoStat.get("avg_rating")).doubleValue() * 
                             ((Number) yogiyoStat.get("review_count")).intValue();
        }
        if (coupangStat != null) {
            totalReviewCount += ((Number) coupangStat.get("review_count")).intValue();
            totalRatingSum += ((Number) coupangStat.get("avg_rating")).doubleValue() * 
                             ((Number) coupangStat.get("review_count")).intValue();
        }
        
        Double totalAvgRating = totalReviewCount > 0 ? 
                Math.round((totalRatingSum / totalReviewCount) * 10.0) / 10.0 : 0.0;
        
        return StoreReviewStatsRespDto.StoreReviewStat.builder()
                .rowNumber(rowNumber)
                .storeName(storeName)
                .totalReviewCount(totalReviewCount)
                .totalAvgRating(totalAvgRating)
                .baeminReviewCount(baeminStat != null ? ((Number) baeminStat.get("review_count")).intValue() : 0)
                .baeminAvgRating(baeminStat != null ? ((Number) baeminStat.get("avg_rating")).doubleValue() : 0.0)
                .yogiyoReviewCount(yogiyoStat != null ? ((Number) yogiyoStat.get("review_count")).intValue() : 0)
                .yogiyoAvgRating(yogiyoStat != null ? ((Number) yogiyoStat.get("avg_rating")).doubleValue() : 0.0)
                .coupangReviewCount(coupangStat != null ? ((Number) coupangStat.get("review_count")).intValue() : 0)
                .coupangAvgRating(coupangStat != null ? ((Number) coupangStat.get("avg_rating")).doubleValue() : 0.0)
                .storePlatformCode(null) // 매장명 기준이므로 특정 코드 없음
                .build();
    }
    
    /**
     * 년월 형식 검증 (YYYYMM)
     */
    private boolean isValidYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.length() != 6) {
            return false;
        }
        
        try {
            int year = Integer.parseInt(yearMonth.substring(0, 4));
            int month = Integer.parseInt(yearMonth.substring(4, 6));
            
            return year >= 2020 && year <= 2030 && month >= 1 && month <= 12;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 기간별 리뷰 상세 조회
     */
    public RespDto<PeriodReviewRespDto> getPeriodReviews(PeriodReviewReqDto reqDto) {
        try {
            log.info("기간별 리뷰조회 시작 - 거래처: {}, 브랜드: {}, 앱: {}, 기간: {}~{}", 
                    reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getAppName(), 
                    reqDto.getStartYm(), reqDto.getEndYm());
            
            // 1. 입력값 검증
            if (reqDto.getHqCode() == null) {
                return RespDto.fail("거래처코드는 필수입니다.");
            }
            if (reqDto.getBrandCode() == null) {
                return RespDto.fail("브랜드코드는 필수입니다.");
            }
            if (reqDto.getAppName() == null || reqDto.getAppName().trim().isEmpty()) {
                return RespDto.fail("앱 이름은 필수입니다.");
            }
            if (reqDto.getStartYm() == null || reqDto.getEndYm() == null) {
                return RespDto.fail("시작년월과 종료년월은 필수입니다.");
            }
            if (!isValidYearMonth(reqDto.getStartYm()) || !isValidYearMonth(reqDto.getEndYm())) {
                return RespDto.fail("년월 형식이 잘못되었습니다. YYYYMM 형식으로 입력해주세요.");
            }
            if (reqDto.getStartYm().compareTo(reqDto.getEndYm()) > 0) {
                return RespDto.fail("시작년월이 종료년월보다 클 수 없습니다.");
            }
            
            // 2. 기간별 리뷰 상세 조회
            List<Map<String, Object>> rawReviews = storeReviewStatsRepository
                    .findPeriodReviews(reqDto.getHqCode(), reqDto.getBrandCode(), 
                                       reqDto.getAppName(), reqDto.getStartYm(), reqDto.getEndYm());
            
            if (rawReviews.isEmpty()) {
                log.info("기간별 리뷰 조회 결과 없음 - 거래처: {}, 브랜드: {}, 앱: {}", 
                        reqDto.getHqCode(), reqDto.getBrandCode(), reqDto.getAppName());
                
                PeriodReviewRespDto emptyResp = PeriodReviewRespDto.builder()
                        .reviewDetails(new ArrayList<>())
                        .totalCount(0)
                        .build();
                
                return RespDto.success("기간별 리뷰조회 완료 (데이터 없음)", emptyResp);
            }
            
            // 3. 리뷰 이미지 조회 (배치로 조회)
            List<Integer> reviewPlatformCodes = rawReviews.stream()
                    .map(review -> ((Number) review.get("review_platform_code")).intValue())
                    .collect(Collectors.toList());
            
            List<Map<String, Object>> reviewImages = storeReviewStatsRepository
                    .findReviewImagesByReviewCodes(reviewPlatformCodes);
            
            // 4. 이미지를 리뷰 코드별로 그룹핑
            Map<Integer, List<String>> imageMap = groupImagesByReviewCode(reviewImages);
            
            // 5. 리뷰 상세 정보 생성
            List<PeriodReviewRespDto.ReviewDetail> reviewDetails = rawReviews.stream()
                    .map(review -> createReviewDetail(review, imageMap))
                    .collect(Collectors.toList());
            
            PeriodReviewRespDto respDto = PeriodReviewRespDto.builder()
                    .reviewDetails(reviewDetails)
                    .totalCount(reviewDetails.size())
                    .build();
            
            String appInfo = "전체".equals(reqDto.getAppName()) ? "전체앱" : reqDto.getAppName();
            String brandInfo = reqDto.getBrandCode() == 0 ? "전체브랜드" : "브랜드(" + reqDto.getBrandCode() + ")";
            
            log.info("기간별 리뷰조회 완료 - 거래처: {}, {}, {}, 총 {}건", 
                    reqDto.getHqCode(), brandInfo, appInfo, reviewDetails.size());
            
            return RespDto.success("기간별 리뷰조회 완료", respDto);
            
        } catch (Exception e) {
            log.error("기간별 리뷰조회 중 오류 발생", e);
            return RespDto.fail("기간별 리뷰조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 이미지를 리뷰 코드별로 그룹핑
     */
    private Map<Integer, List<String>> groupImagesByReviewCode(List<Map<String, Object>> reviewImages) {
        Map<Integer, List<String>> imageMap = new HashMap<>();
        
        for (Map<String, Object> image : reviewImages) {
            Integer reviewPlatformCode = ((Number) image.get("review_platform_code")).intValue();
            String imageUrl = (String) image.get("image_url");
            
            imageMap.computeIfAbsent(reviewPlatformCode, k -> new ArrayList<>()).add(imageUrl);
        }
        
        return imageMap;
    }
    
    /**
     * 개별 리뷰 상세 정보 생성
     */
    private PeriodReviewRespDto.ReviewDetail createReviewDetail(
            Map<String, Object> review, Map<Integer, List<String>> imageMap) {
        
        Integer reviewPlatformCode = ((Number) review.get("review_platform_code")).intValue();
        
        // 날짜 처리
        LocalDate reviewDate = null;
        Object reviewDateObj = review.get("review_date");
        if (reviewDateObj != null) {
            if (reviewDateObj instanceof java.sql.Date) {
                reviewDate = ((java.sql.Date) reviewDateObj).toLocalDate();
            } else if (reviewDateObj instanceof java.time.LocalDate) {
                reviewDate = (LocalDate) reviewDateObj;
            }
        }
        
        // 별점 처리
        Integer rating = 0;
        Object ratingObj = review.get("rating");
        if (ratingObj != null && ratingObj instanceof Number) {
            rating = ((Number) ratingObj).intValue();
        }
        
        // 리뷰 이미지 처리
        List<String> reviewImages = imageMap.getOrDefault(reviewPlatformCode, new ArrayList<>());
        
        return PeriodReviewRespDto.ReviewDetail.builder()
                .storeName((String) review.get("store_name"))
                .reviewDate(reviewDate)
                .rating(rating)
                .orderMenu((String) review.get("order_menu"))
                .content((String) review.get("content"))
                .reviewImages(reviewImages)
                .ownerReplyContent((String) review.get("owner_reply_content"))
                .platform((String) review.get("platform"))
                .reviewPlatformCode(reviewPlatformCode)
                .build();
    }
}