package com.inc.sh.service.platform;

import com.inc.sh.repository.DashboardSalesRepository;
import com.inc.sh.repository.StoreReviewStatsRepository;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.DashboardSalesReqDto;
import com.inc.sh.dto.platform.respDto.DashboardSalesRespDto;
import com.inc.sh.dto.platform.respDto.DashboardReviewRespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformDashboardService {
    
    private final DashboardSalesRepository dashboardSalesRepository;
    private final StoreReviewStatsRepository storeReviewStatsRepository;
    
    /**
     * 대시보드 매출 그래프 조회
     */
    public RespDto<DashboardSalesRespDto> getDashboardSales(DashboardSalesReqDto reqDto) {
        try {
            log.info("대시보드 매출 그래프 조회 시작 - 본사: {}, 브랜드: {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode());
            
            // 현재 날짜 기준 월 계산
            LocalDate now = LocalDate.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month1Ago = now.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month2Ago = now.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month11Ago = now.minusMonths(11).format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month4Ago = now.minusMonths(4).format(DateTimeFormatter.ofPattern("yyyyMM"));
            
            log.info("조회 기간 - 3개월: {} ~ {}, 12개월: {} ~ {}, 5개월: {} ~ {}", 
                    month2Ago, currentMonth, month11Ago, currentMonth, month4Ago, currentMonth);
            
            // 1. 3개월 매출액 조회
            List<Object[]> sales3MonthResults = dashboardSalesRepository.find3MonthSales(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    currentMonth, month1Ago, month2Ago);
            
            // 2. 3개월 주문수 조회
            List<Object[]> order3MonthResults = dashboardSalesRepository.find3MonthOrders(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    currentMonth, month1Ago, month2Ago);
            
            // 3. 12개월 매출액 추이 조회
            List<Object[]> sales12MonthResults = dashboardSalesRepository.find12MonthSales(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    month11Ago, currentMonth);
            
            // 4. 12개월 주문수 추이 조회
            List<Object[]> order12MonthResults = dashboardSalesRepository.find12MonthOrders(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    month11Ago, currentMonth);
            
            // 5. 5개월 플랫폼별 매출 조회 (배달분석)
            List<Object[]> platform5MonthResults = dashboardSalesRepository.find5MonthPlatformSales(
                    reqDto.getHqCode(), reqDto.getBrandCode(), 
                    month4Ago, currentMonth);
            
            // 응답 데이터 구성
            DashboardSalesRespDto response = DashboardSalesRespDto.builder()
                    .sales3Month(convertToMonthlyData(sales3MonthResults, Arrays.asList(month2Ago, month1Ago, currentMonth)))
                    .order3Month(convertToMonthlyData(order3MonthResults, Arrays.asList(month2Ago, month1Ago, currentMonth)))
                    .sales12Month(convertToMonthlyData(sales12MonthResults, generate12MonthList(now)))
                    .order12Month(convertToMonthlyData(order12MonthResults, generate12MonthList(now)))
                    .deliveryAnalysis(convertToDeliveryRateData(platform5MonthResults, generate5MonthList(now)))
                    .build();
            
            log.info("대시보드 매출 그래프 조회 완료 - 3개월: {}건, 12개월: {}건, 배달분석: {}건", 
                    sales3MonthResults.size(), sales12MonthResults.size(), platform5MonthResults.size());
            
            return RespDto.success("대시보드 매출 그래프 조회 완료", response);
            
        } catch (Exception e) {
            log.error("대시보드 매출 그래프 조회 중 오류 발생", e);
            return RespDto.fail("대시보드 매출 그래프 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 대시보드 리뷰 종합 정보 조회 (5가지 한번에)
     */
    public RespDto<DashboardReviewRespDto> getDashboardReviews(DashboardSalesReqDto reqDto) {
        try {
            log.info("대시보드 리뷰 종합 정보 조회 시작 - 본사: {}, 브랜드: {}", 
                    reqDto.getHqCode(), reqDto.getBrandCode());
            
            LocalDate now = LocalDate.now();
            String currentMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
            String month2Ago = now.minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"));  // 3개월 (현재월 포함)
            String month11Ago = now.minusMonths(11).format(DateTimeFormatter.ofPattern("yyyyMM")); // 12개월
            
            log.info("리뷰 조회 기간 - 3개월: {} ~ {}, 12개월: {} ~ {}, 현재월: {}", 
                    month2Ago, currentMonth, month11Ago, currentMonth, currentMonth);
            
            // 1. 채널별 리뷰 비율 (3개월)
            List<Map<String, Object>> channelCounts = storeReviewStatsRepository
                    .findChannelReviewCounts(reqDto.getHqCode(), reqDto.getBrandCode(), month2Ago, currentMonth);
            
            // 2. 채널별 별점평균 (3개월)
            List<Map<String, Object>> channelRatings = storeReviewStatsRepository
                    .findChannelRatings(reqDto.getHqCode(), reqDto.getBrandCode(), month2Ago, currentMonth);
            
            // 3. 월별 채널별 리뷰추이 (12개월)
            List<Map<String, Object>> monthlyTrend = storeReviewStatsRepository
                    .findMonthlyChannelTrend(reqDto.getHqCode(), reqDto.getBrandCode(), month11Ago, currentMonth);
            
            // 4. 평점 높은 매장 순위 (현재월, Top 10)
            List<Map<String, Object>> topRatedStores = storeReviewStatsRepository
                    .findTopRatedStores(reqDto.getHqCode(), reqDto.getBrandCode(), currentMonth);
            
            // 5. 리뷰 많은 매장 순위 (현재월, Top 10)
            List<Map<String, Object>> topReviewedStores = storeReviewStatsRepository
                    .findTopReviewedStores(reqDto.getHqCode(), reqDto.getBrandCode(), currentMonth);
            
            // 데이터 가공
            DashboardReviewRespDto response = DashboardReviewRespDto.builder()
                    .channelRatio(createChannelRatio(channelCounts))
                    .channelRatings(createChannelRatings(channelRatings))
                    .channelTrend(createMonthlyTrend(monthlyTrend, generate12MonthList(now)))
                    .topRatedStores(createStoreRankings(topRatedStores, true))  // true = 평점순위
                    .topReviewedStores(createStoreRankings(topReviewedStores, false)) // false = 리뷰수순위
                    .build();
            
            log.info("대시보드 리뷰 종합 정보 조회 완료 - 채널비율: {}개, 채널별점: {}개, 추이: {}개, 평점순위: {}개, 리뷰순위: {}개", 
                    channelCounts.size(), channelRatings.size(), monthlyTrend.size(), 
                    topRatedStores.size(), topReviewedStores.size());
            
            return RespDto.success("대시보드 리뷰 종합 정보 조회 완료", response);
            
        } catch (Exception e) {
            log.error("대시보드 리뷰 종합 정보 조회 중 오류 발생", e);
            return RespDto.fail("대시보드 리뷰 종합 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 채널별 리뷰 비율 데이터 생성
     */
    private DashboardReviewRespDto.ChannelRatio createChannelRatio(List<Map<String, Object>> channelCounts) {
        
        Map<String, Integer> countMap = channelCounts.stream()
                .collect(Collectors.toMap(
                        result -> (String) result.get("platform"),
                        result -> ((Number) result.get("review_count")).intValue(),
                        (existing, replacement) -> existing
                ));
        
        Integer baeminCount = countMap.getOrDefault("배민", 0);
        Integer yogiyoCount = countMap.getOrDefault("요기요", 0);
        Integer coupangCount = countMap.getOrDefault("쿠팡이츠", 0);
        Integer totalCount = baeminCount + yogiyoCount + coupangCount;
        
        // 비율 계산
        String baeminRate = calculatePercentage(baeminCount, totalCount);
        String yogiyoRate = calculatePercentage(yogiyoCount, totalCount);
        String coupangRate = calculatePercentage(coupangCount, totalCount);
        
        return DashboardReviewRespDto.ChannelRatio.builder()
                .baeminCount(baeminCount)
                .baeminRate(baeminRate)
                .yogiyoCount(yogiyoCount)
                .yogiyoRate(yogiyoRate)
                .coupangCount(coupangCount)
                .coupangRate(coupangRate)
                .totalCount(totalCount)
                .build();
    }
    
    /**
     * 채널별 별점평균 데이터 생성
     */
    private DashboardReviewRespDto.ChannelRatings createChannelRatings(List<Map<String, Object>> channelRatings) {
        
        Map<String, Double> ratingMap = channelRatings.stream()
                .collect(Collectors.toMap(
                        result -> (String) result.get("platform"),
                        result -> ((Number) result.get("avg_rating")).doubleValue(),
                        (existing, replacement) -> existing
                ));
        
        return DashboardReviewRespDto.ChannelRatings.builder()
                .baeminRating(ratingMap.getOrDefault("배민", 0.0))
                .yogiyoRating(ratingMap.getOrDefault("요기요", 0.0))
                .coupangRating(ratingMap.getOrDefault("쿠팡이츠", 0.0))
                .build();
    }
    
    /**
     * 월별 리뷰추이 데이터 생성
     */
    private List<DashboardReviewRespDto.MonthlyReviewTrend> createMonthlyTrend(
            List<Map<String, Object>> monthlyTrend, List<String> monthList) {
        
        // 월별 플랫폼별 리뷰수를 Map으로 구성
        Map<String, Map<String, Integer>> monthPlatformMap = new HashMap<>();
        
        for (Map<String, Object> result : monthlyTrend) {
            String month = (String) result.get("month");
            String platform = (String) result.get("platform");
            Integer count = ((Number) result.get("review_count")).intValue();
            
            monthPlatformMap
                    .computeIfAbsent(month, k -> new HashMap<>())
                    .put(platform, count);
        }
        
        // monthList 순서대로 데이터 생성
        List<DashboardReviewRespDto.MonthlyReviewTrend> trendList = new ArrayList<>();
        
        for (String month : monthList) {
            Map<String, Integer> platformCounts = monthPlatformMap.getOrDefault(month, new HashMap<>());
            
            Integer baeminCount = platformCounts.getOrDefault("배민", 0);
            Integer yogiyoCount = platformCounts.getOrDefault("요기요", 0);
            Integer coupangCount = platformCounts.getOrDefault("쿠팡이츠", 0);
            Integer totalCount = baeminCount + yogiyoCount + coupangCount;
            
            String koreanMonth = convertToKoreanMonth(month);
            
            DashboardReviewRespDto.MonthlyReviewTrend trend = DashboardReviewRespDto.MonthlyReviewTrend.builder()
                    .month(koreanMonth)
                    .baeminCount(baeminCount)
                    .yogiyoCount(yogiyoCount)
                    .coupangCount(coupangCount)
                    .totalCount(totalCount)
                    .build();
            
            trendList.add(trend);
        }
        
        return trendList;
    }
    
    /**
     * 매장 순위 데이터 생성
     */
    private List<DashboardReviewRespDto.StoreRanking> createStoreRankings(
            List<Map<String, Object>> storeData, boolean isRatingRank) {
        
        List<DashboardReviewRespDto.StoreRanking> rankings = new ArrayList<>();
        
        for (int i = 0; i < storeData.size(); i++) {
            Map<String, Object> store = storeData.get(i);
            
            String storeName = (String) store.get("store_name");
            Double rating = store.get("avg_rating") != null ? 
                    ((Number) store.get("avg_rating")).doubleValue() : 0.0;
            Integer reviewCount = store.get("review_count") != null ? 
                    ((Number) store.get("review_count")).intValue() : 0;
            
            DashboardReviewRespDto.StoreRanking ranking = DashboardReviewRespDto.StoreRanking.builder()
                    .rank(i + 1)  // 순위는 1부터 시작
                    .storeName(storeName)
                    .rating(isRatingRank ? rating : null)  // 평점순위면 평점, 리뷰순위면 null
                    .reviewCount(isRatingRank ? null : reviewCount)  // 리뷰순위면 리뷰수, 평점순위면 null
                    .build();
            
            rankings.add(ranking);
        }
        
        return rankings;
    }
    
    /**
     * 쿼리 결과를 MonthlyData 리스트로 변환 (과거월 먼저, 현재월 마지막)
     */
    private List<DashboardSalesRespDto.MonthlyData> convertToMonthlyData(List<Object[]> results, List<String> monthList) {
        
        // 쿼리 결과를 Map으로 변환 (월 → 값)
        Map<String, Long> resultMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],  // 월
                        result -> ((Number) result[1]).longValue(),  // 값
                        (existing, replacement) -> existing
                ));
        
        // monthList 순서대로 데이터 생성 (없으면 0)
        List<DashboardSalesRespDto.MonthlyData> monthlyDataList = new ArrayList<>();
        
        for (String month : monthList) {
            Long value = resultMap.getOrDefault(month, 0L);
            String koreanMonth = convertToKoreanMonth(month);
            
            DashboardSalesRespDto.MonthlyData monthlyData = DashboardSalesRespDto.MonthlyData.builder()
                    .month(koreanMonth)
                    .value(value)
                    .build();
            
            monthlyDataList.add(monthlyData);
        }
        
        return monthlyDataList;
    }
    
    /**
     * 12개월 리스트 생성 (과거 11개월 + 현재월)
     */
    private List<String> generate12MonthList(LocalDate currentDate) {
        List<String> monthList = new ArrayList<>();
        
        // 11개월 전부터 현재월까지
        for (int i = 11; i >= 0; i--) {
            String month = currentDate.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
            monthList.add(month);
        }
        
        return monthList;
    }
    
    /**
     * 5개월 리스트 생성 (과거 4개월 + 현재월)
     */
    private List<String> generate5MonthList(LocalDate currentDate) {
        List<String> monthList = new ArrayList<>();
        
        // 4개월 전부터 현재월까지
        for (int i = 4; i >= 0; i--) {
            String month = currentDate.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyyMM"));
            monthList.add(month);
        }
        
        return monthList;
    }
    
    /**
     * 플랫폼별 매출을 배달앱 비율 데이터로 변환
     */
    private List<DashboardSalesRespDto.DeliveryRateData> convertToDeliveryRateData(List<Object[]> results, List<String> monthList) {
        
        // 월별 플랫폼별 매출 맵 구성
        Map<String, Map<String, Long>> monthPlatformSalesMap = new HashMap<>();
        
        // 쿼리 결과를 월별 플랫폼별로 그룹핑
        for (Object[] result : results) {
            String month = (String) result[0];      // 월
            String platform = (String) result[1];   // 플랫폼
            Long sales = ((Number) result[2]).longValue(); // 매출
            
            monthPlatformSalesMap
                    .computeIfAbsent(month, k -> new HashMap<>())
                    .put(platform, sales);
        }
        
        // monthList 순서대로 데이터 생성
        List<DashboardSalesRespDto.DeliveryRateData> deliveryRateList = new ArrayList<>();
        
        for (String month : monthList) {
            Map<String, Long> platformSales = monthPlatformSalesMap.getOrDefault(month, new HashMap<>());
            
            // 각 플랫폼 매출 (없으면 0)
            Long baeminSales = platformSales.getOrDefault("배민", 0L);
            Long yogiyoSales = platformSales.getOrDefault("요기요", 0L);
            Long coupangSales = platformSales.getOrDefault("쿠팡이츠", 0L);
            
            // 전체 매출 계산
            Long totalSales = baeminSales + yogiyoSales + coupangSales;
            
            // 비율 계산 (소수점 1자리)
            String baeminRate = calculatePercentage(baeminSales, totalSales);
            String yogiyoRate = calculatePercentage(yogiyoSales, totalSales);
            String coupangRate = calculatePercentage(coupangSales, totalSales);
            
            String koreanMonth = convertToKoreanMonth(month);
            
            DashboardSalesRespDto.DeliveryRateData deliveryRateData = DashboardSalesRespDto.DeliveryRateData.builder()
                    .month(koreanMonth)
                    .baeminRate(baeminRate)
                    .coupangRate(coupangRate)
                    .yogiyoRate(yogiyoRate)
                    .build();
            
            deliveryRateList.add(deliveryRateData);
        }
        
        return deliveryRateList;
    }
    
    /**
     * 비율 계산 (소수점 1자리 + %)
     */
    private String calculatePercentage(Number partValue, Number totalValue) {
        if (totalValue == null || totalValue.longValue() == 0) {
            return "0.0%";
        }
        
        double percentage = (partValue.doubleValue() / totalValue.doubleValue()) * 100;
        return String.format("%.1f%%", percentage);
    }
    
    /**
     * YYYYMM 형식을 한글 형식으로 변환
     * "202601" → "2026년 1월"
     */
    private String convertToKoreanMonth(String yyyyMM) {
        try {
            String year = yyyyMM.substring(0, 4);
            String month = yyyyMM.substring(4, 6);
            
            // 앞의 0 제거
            int monthInt = Integer.parseInt(month);
            
            return year + "년 " + monthInt + "월";
        } catch (Exception e) {
            log.warn("월 형식 변환 오류 - 입력값: {}", yyyyMM, e);
            return yyyyMM; // 변환 실패시 원본 반환
        }
    }
}