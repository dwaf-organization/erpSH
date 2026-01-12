package com.inc.sh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.inc.sh.entity.OrderPlatforms;

import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<OrderPlatforms, Integer> {

    /**
     * 일별매출 조회 - 거래처별 + 일별 집계 (전체 플랫폼 합계)
     * @param hqCode 본사코드
     * @param brandCode 브랜드코드 (0이면 전체)
     * @param startDate 시작일 (YYYYMMDD)
     * @param endDate 종료일 (YYYYMMDD)
     */
    @Query(value = 
        "SELECT " +
        "    sp.customer_code, " +                // 0: 거래처코드
        "    c.customer_name, " +                  // 1: 거래처명
        "    SUBSTRING(op.order_date, 7, 2) AS day_of_month, " + // 2: 일 (01~31)
        "    COALESCE(SUM(op.order_amount), 0) AS daily_sales " + // 3: 일매출 (전체 플랫폼 합계)
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "LEFT JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "    AND op.order_date >= :startDate " +
        "    AND op.order_date <= :endDate " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "GROUP BY sp.customer_code, c.customer_name, SUBSTRING(op.order_date, 7, 2) " +
        "ORDER BY sp.customer_code, SUBSTRING(op.order_date, 7, 2)",
        nativeQuery = true)
    List<Object[]> findDailySalesByMonth(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 거래처별 월 총매출 조회 (일별매출 응답에서 월합계 사용) - 전체 플랫폼 합계
     */
    @Query(value = 
        "SELECT " +
        "    sp.customer_code, " +                // 0: 거래처코드
        "    c.customer_name, " +                  // 1: 거래처명
        "    COALESCE(SUM(op.order_amount), 0) AS monthly_total " + // 2: 월총매출 (전체 플랫폼 합계)
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "LEFT JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "    AND op.order_date >= :startDate " +
        "    AND op.order_date <= :endDate " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "GROUP BY sp.customer_code, c.customer_name " +
        "ORDER BY sp.customer_code",
        nativeQuery = true)
    List<Object[]> findMonthlyTotalByStore(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 월간매출 조회 - 거래처별 + 월별 집계 (전체 플랫폼 합계)
     */
    @Query(value = 
        "SELECT " +
        "    sp.customer_code, " +                // 0: 거래처코드
        "    c.customer_name, " +                  // 1: 거래처명
        "    SUBSTRING(op.order_date, 1, 6) AS month, " + // 2: 월 (YYYYMM)
        "    COALESCE(SUM(op.order_amount), 0) AS total_sales, " + // 3: 총매출 (전체 플랫폼 합계)
        "    COALESCE(COUNT(op.order_platform_code), 0) AS order_count " + // 4: 주문수 (전체 플랫폼 합계)
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "LEFT JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "    AND SUBSTRING(op.order_date, 1, 6) >= :startMonth " +
        "    AND SUBSTRING(op.order_date, 1, 6) <= :endMonth " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "GROUP BY sp.customer_code, c.customer_name, SUBSTRING(op.order_date, 1, 6) " +
        "ORDER BY sp.customer_code, SUBSTRING(op.order_date, 1, 6)",
        nativeQuery = true)
    List<Object[]> findMonthlySalesByStore(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );
}