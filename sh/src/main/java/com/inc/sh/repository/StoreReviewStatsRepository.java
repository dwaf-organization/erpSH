package com.inc.sh.repository;

import com.inc.sh.entity.StorePlatforms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface StoreReviewStatsRepository extends JpaRepository<StorePlatforms, Integer> {

    /**
     * 매장별 리뷰 통계 조회 (기간별, 플랫폼별) - 매장명으로 그룹핑
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param startYearMonth 시작년월 (YYYYMM)
     * @param endYearMonth 종료년월 (YYYYMM)
     * @return 매장별 플랫폼별 리뷰 통계
     */
    @Query(value = 
        "SELECT " +
        "    sp.platform_store_name as store_name, " +
        "    rp.platform, " +
        "    COUNT(*) as review_count, " +
        "    ROUND(AVG(CAST(rp.rating AS DECIMAL(3,2))), 1) as avg_rating " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYearMonth AND :endYearMonth " +
        "GROUP BY sp.platform_store_name, rp.platform " +
        "ORDER BY sp.platform_store_name, rp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findStoreReviewStatsByPeriod(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startYearMonth") String startYearMonth,
        @Param("endYearMonth") String endYearMonth
    );

    /**
     * 전체 합계 리뷰 통계 조회 (기간별, 플랫폼별)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param startYearMonth 시작년월 (YYYYMM)
     * @param endYearMonth 종료년월 (YYYYMM)
     * @return 전체 플랫폼별 리뷰 합계
     */
    @Query(value = 
        "SELECT " +
        "    rp.platform, " +
        "    COUNT(*) as review_count, " +
        "    ROUND(AVG(CAST(rp.rating AS DECIMAL(3,2))), 1) as avg_rating " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYearMonth AND :endYearMonth " +
        "GROUP BY rp.platform " +
        "ORDER BY rp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findTotalReviewStatsByPeriod(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startYearMonth") String startYearMonth,
        @Param("endYearMonth") String endYearMonth
    );

    /**
     * 매장 목록 조회 (거래처별) - 매장명으로 중복 제거
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @return 매장 목록 (매장명만, 중복 제거)
     */
    @Query(value = 
        "SELECT DISTINCT " +
        "    sp.platform_store_name as store_name " +
        "FROM store_platforms sp " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "ORDER BY sp.platform_store_name",
        nativeQuery = true)
    List<Map<String, Object>> findStoresByHqCode(
        @Param("hqCode") Integer hqCode, 
        @Param("brandCode") Integer brandCode
    );

    /**
     * 기간별 리뷰 상세 조회
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param appName 앱 이름 (전체면 조건 제외)
     * @param startYm 시작년월 (YYYYMM)
     * @param endYm 종료년월 (YYYYMM)
     * @return 리뷰 상세 목록
     */
    @Query(value = 
        "SELECT " +
        "    rp.review_platform_code, " +
        "    sp.platform_store_name as store_name, " +
        "    rp.review_date, " +
        "    rp.rating, " +
        "    rp.order_menu, " +
        "    rp.content, " +
        "    rp.owner_reply_content, " +
        "    rp.platform " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND (:appName = '전체' OR rp.platform = :appName) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYm AND :endYm " +
        "ORDER BY rp.review_date DESC, rp.review_platform_code DESC",
        nativeQuery = true)
    List<Map<String, Object>> findPeriodReviews(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("appName") String appName,
        @Param("startYm") String startYm,
        @Param("endYm") String endYm
    );

    /**
     * 특정 리뷰들의 이미지 조회
     * 
     * @param reviewPlatformCodes 리뷰 코드 리스트
     * @return 리뷰별 이미지 URL 목록
     */
    @Query(value = 
        "SELECT " +
        "    rip.review_platform_code, " +
        "    rip.image_url " +
        "FROM review_image_platform rip " +
        "WHERE rip.review_platform_code IN (:reviewPlatformCodes) " +
        "ORDER BY rip.review_platform_code, rip.seq",
        nativeQuery = true)
    List<Map<String, Object>> findReviewImagesByReviewCodes(
        @Param("reviewPlatformCodes") List<Integer> reviewPlatformCodes
    );

    // ==================== 리뷰 대시보드 쿼리 ====================
    
    /**
     * 채널별 리뷰 수 (3개월)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param startYearMonth 시작년월 (3개월 전)
     * @param endYearMonth 종료년월 (현재월)
     * @return 플랫폼별 리뷰수
     */
    @Query(value = 
        "SELECT " +
        "    rp.platform, " +
        "    COUNT(*) as review_count " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYearMonth AND :endYearMonth " +
        "GROUP BY rp.platform " +
        "ORDER BY rp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findChannelReviewCounts(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startYearMonth") String startYearMonth,
        @Param("endYearMonth") String endYearMonth
    );
    
    /**
     * 채널별 평점 평균 (3개월)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param startYearMonth 시작년월 (3개월 전)
     * @param endYearMonth 종료년월 (현재월)
     * @return 플랫폼별 평점평균
     */
    @Query(value = 
        "SELECT " +
        "    rp.platform, " +
        "    ROUND(AVG(CAST(rp.rating AS DECIMAL(3,2))), 1) as avg_rating " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYearMonth AND :endYearMonth " +
        "GROUP BY rp.platform " +
        "ORDER BY rp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findChannelRatings(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startYearMonth") String startYearMonth,
        @Param("endYearMonth") String endYearMonth
    );
    
    /**
     * 월별 채널별 리뷰 추이 (12개월)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param startYearMonth 시작년월 (12개월 전)
     * @param endYearMonth 종료년월 (현재월)
     * @return 월별 플랫폼별 리뷰수
     */
    @Query(value = 
        "SELECT " +
        "    DATE_FORMAT(rp.review_date, '%Y%m') as month, " +
        "    rp.platform, " +
        "    COUNT(*) as review_count " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') BETWEEN :startYearMonth AND :endYearMonth " +
        "GROUP BY DATE_FORMAT(rp.review_date, '%Y%m'), rp.platform " +
        "ORDER BY month, rp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findMonthlyChannelTrend(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startYearMonth") String startYearMonth,
        @Param("endYearMonth") String endYearMonth
    );
    
    /**
     * 평점 높은 매장 순위 (현재월, Top 10)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param currentMonth 현재월
     * @return 평점 높은 매장 순위
     */
    @Query(value = 
        "SELECT " +
        "    sp.platform_store_name as store_name, " +
        "    ROUND(AVG(CAST(rp.rating AS DECIMAL(3,2))), 1) as avg_rating, " +
        "    COUNT(*) as review_count " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') = :currentMonth " +
        "GROUP BY sp.platform_store_name " +
        "HAVING COUNT(*) >= 3 " +  // 최소 3개 이상 리뷰 있는 매장만
        "ORDER BY avg_rating DESC, review_count DESC " +
        "LIMIT 10",
        nativeQuery = true)
    List<Map<String, Object>> findTopRatedStores(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("currentMonth") String currentMonth
    );
    
    /**
     * 리뷰 많은 매장 순위 (현재월, Top 10)
     * 
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param currentMonth 현재월
     * @return 리뷰 많은 매장 순위
     */
    @Query(value = 
        "SELECT " +
        "    sp.platform_store_name as store_name, " +
        "    COUNT(*) as review_count, " +
        "    ROUND(AVG(CAST(rp.rating AS DECIMAL(3,2))), 1) as avg_rating " +
        "FROM review_platform rp " +
        "INNER JOIN store_platforms sp ON rp.store_platform_code = sp.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "  AND sp.is_active = true " +
        "  AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "  AND DATE_FORMAT(rp.review_date, '%Y%m') = :currentMonth " +
        "GROUP BY sp.platform_store_name " +
        "ORDER BY review_count DESC, avg_rating DESC " +
        "LIMIT 10",
        nativeQuery = true)
    List<Map<String, Object>> findTopReviewedStores(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("currentMonth") String currentMonth
    );
}