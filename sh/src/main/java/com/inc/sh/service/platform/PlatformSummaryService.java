package com.inc.sh.service.platform;

import com.inc.sh.entity.DailySalesSummary;
import com.inc.sh.entity.StoreRankingCache;
import com.inc.sh.repository.DailySalesSummaryRepository;
import com.inc.sh.repository.StoreRankingCacheRepository;
import com.inc.sh.repository.OrderPlatformsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformSummaryService {
    
    private final OrderPlatformsRepository orderPlatformsRepository;
    private final DailySalesSummaryRepository dailySalesSummaryRepository;
    private final StoreRankingCacheRepository storeRankingCacheRepository;
    
    /**
     * 일별 매출 집계 테이블 갱신
     */
    @Transactional
    public void updateDailySalesSummary(String platform, Integer brandCode) {
        try {
            log.info("일별 매출 집계 갱신 시작 - 플랫폼: {}, 브랜드: {}", platform, brandCode);
            
            // 최근 7일간 데이터 집계
            LocalDate today = LocalDate.now();
            for (int i = 0; i < 7; i++) {
                LocalDate targetDate = today.minusDays(i);
                String summaryDate = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                
                // 해당일의 플랫폼별 주문 집계 조회
                Object[] result = orderPlatformsRepository.findDailySummaryByPlatformAndDate(
                    platform, summaryDate, brandCode);
                
                if (result != null && result.length >= 2) {
                    Integer orderCount = ((Number) result[0]).intValue();
                    Integer totalAmount = ((Number) result[1]).intValue();
                    
                    // 기존 집계 데이터 확인 및 갱신
                    DailySalesSummary existing = dailySalesSummaryRepository
                        .findByPlatformAndSummaryDateAndBrandCode(platform, summaryDate, brandCode);
                    
                    if (existing != null) {
                        // 기존 데이터 갱신
                        existing.setOrderCount(orderCount);
                        existing.setTotalOrderAmount(totalAmount);
                        existing.setTotalSettleAmount(totalAmount); // 일단 동일하게 설정
                        dailySalesSummaryRepository.save(existing);
                        log.debug("일별 집계 갱신 - 날짜: {}, 플랫폼: {}, 주문: {}건, 금액: {}원", 
                                summaryDate, platform, orderCount, totalAmount);
                    } else if (orderCount > 0) {
                        // 새로운 집계 데이터 생성
                        DailySalesSummary newSummary = DailySalesSummary.builder()
                                .storePlatformCode(0) // 플랫폼 전체 집계
                                .customerCode(0) // 전체 거래처
                                .brandCode(brandCode)
                                .platform(platform)
                                .summaryDate(summaryDate)
                                .orderCount(orderCount)
                                .totalOrderAmount(totalAmount)
                                .totalSettleAmount(totalAmount)
                                .totalDeliveryAmount(0)
                                .totalDiscountAmount(0)
                                .totalFee(0)
                                .description("하이픈 API 자동 집계")
                                .build();
                        
                        dailySalesSummaryRepository.save(newSummary);
                        log.debug("일별 집계 신규 생성 - 날짜: {}, 플랫폼: {}, 주문: {}건, 금액: {}원", 
                                summaryDate, platform, orderCount, totalAmount);
                    }
                }
            }
            
            log.info("일별 매출 집계 갱신 완료 - 플랫폼: {}, 브랜드: {}", platform, brandCode);
            
        } catch (Exception e) {
            log.error("일별 매출 집계 갱신 실패 - 플랫폼: {}, 브랜드: {}", platform, brandCode, e);
        }
    }
    
    /**
     * 매장 순위 캐시 갱신 
     */
    @Transactional
    public void updateStoreRanking(String platform, Integer brandCode) {
        try {
            log.info("매장 순위 갱신 시작 - 플랫폼: {}, 브랜드: {}", platform, brandCode);
            
            // 최근 7일 기준 매장별 순위 계산
            updateStoreRankingByType(platform, brandCode, "매출순위", "최근7일");
            updateStoreRankingByType(platform, brandCode, "주문수순위", "최근7일");
            
            log.info("매장 순위 갱신 완료 - 플랫폼: {}, 브랜드: {}", platform, brandCode);
            
        } catch (Exception e) {
            log.error("매장 순위 갱신 실패 - 플랫폼: {}, 브랜드: {}", platform, brandCode, e);
        }
    }
    
    /**
     * 순위 유형별 매장 순위 갱신
     */
    private void updateStoreRankingByType(String platform, Integer brandCode, String rankingType, String period) {
        try {
            // 7일전 날짜 계산
            LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
            String startDate = sevenDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            List<Object[]> rankings;
            
            if ("매출순위".equals(rankingType)) {
                // 매출액 기준 순위
                rankings = orderPlatformsRepository.findStoreRankingByAmount(platform, startDate, brandCode);
            } else {
                // 주문수 기준 순위
                rankings = orderPlatformsRepository.findStoreRankingByOrderCount(platform, startDate, brandCode);
            }
            
            // 기존 캐시 데이터 삭제 (해당 플랫폼, 유형, 기간)
            storeRankingCacheRepository.deleteByPlatformAndRankingTypeAndRankingPeriodAndBrandCode(
                platform, rankingType, period, brandCode);
            
            // 새로운 순위 데이터 저장
            int rank = 1;
            for (Object[] ranking : rankings) {
                if (ranking.length >= 4) {
                    Integer storePlatformCode = ((Number) ranking[0]).intValue();
                    Integer customerCode = ((Number) ranking[1]).intValue();
                    String storeName = (String) ranking[2];
                    Number metricValue = (Number) ranking[3];
                    
                    StoreRankingCache rankingCache = StoreRankingCache.builder()
                            .storePlatformCode(storePlatformCode)
                            .customerCode(customerCode)
                            .brandCode(brandCode)
                            .rankingType(rankingType)
                            .rankingPeriod(period)
                            .platform(platform)
                            .storeName(storeName)
                            .metricValue(BigDecimal.valueOf(metricValue.doubleValue()))
                            .rankNumber(rank)
                            .lastUpdatedAt(LocalDateTime.now())
                            .description("하이픈 API 자동 집계")
                            .build();
                    
                    storeRankingCacheRepository.save(rankingCache);
                    
                    log.debug("매장 순위 저장 - 순위: {}, 매장: {}, 값: {}", rank, storeName, metricValue);
                    rank++;
                }
            }
            
        } catch (Exception e) {
            log.error("매장 순위 갱신 실패 - 플랫폼: {}, 유형: {}", platform, rankingType, e);
        }
    }
}