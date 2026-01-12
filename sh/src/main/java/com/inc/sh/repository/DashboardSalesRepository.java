package com.inc.sh.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.inc.sh.entity.OrderPlatforms;

import java.util.List;

@Repository
public interface DashboardSalesRepository extends JpaRepository<OrderPlatforms, Integer> {

    /**
     * 3개월 매출액 조회 (현재월, -1월, -2월)
     * @param hqCode 본사코드
     * @param brandCode 브랜드코드 (0이면 전체)
     * @param month1 현재월 (YYYYMM)
     * @param month2 현재-1월 (YYYYMM)
     * @param month3 현재-2월 (YYYYMM)
     */
    @Query(value = 
        "SELECT " +
        "    SUBSTRING(op.order_date, 1, 6) AS month, " +    // 0: 월 (YYYYMM)
        "    COALESCE(SUM(op.order_amount), 0) AS total_sales " + // 1: 총매출
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "INNER JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "    AND SUBSTRING(op.order_date, 1, 6) IN (:month1, :month2, :month3) " +
        "GROUP BY SUBSTRING(op.order_date, 1, 6) " +
        "ORDER BY SUBSTRING(op.order_date, 1, 6)",
        nativeQuery = true)
    List<Object[]> find3MonthSales(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("month1") String month1,
        @Param("month2") String month2,
        @Param("month3") String month3
    );

    /**
     * 3개월 주문수 조회 (현재월, -1월, -2월)
     */
    @Query(value = 
        "SELECT " +
        "    SUBSTRING(op.order_date, 1, 6) AS month, " +    // 0: 월 (YYYYMM)
        "    COUNT(op.order_platform_code) AS total_orders " + // 1: 총주문수
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "INNER JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "    AND SUBSTRING(op.order_date, 1, 6) IN (:month1, :month2, :month3) " +
        "GROUP BY SUBSTRING(op.order_date, 1, 6) " +
        "ORDER BY SUBSTRING(op.order_date, 1, 6)",
        nativeQuery = true)
    List<Object[]> find3MonthOrders(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("month1") String month1,
        @Param("month2") String month2,
        @Param("month3") String month3
    );

    /**
     * 12개월 매출액 추이 조회
     * @param hqCode 본사코드
     * @param brandCode 브랜드코드 (0이면 전체)
     * @param startMonth 시작월 (12개월 전, YYYYMM)
     * @param endMonth 종료월 (현재월, YYYYMM)
     */
    @Query(value = 
        "SELECT " +
        "    SUBSTRING(op.order_date, 1, 6) AS month, " +    // 0: 월 (YYYYMM)
        "    COALESCE(SUM(op.order_amount), 0) AS total_sales " + // 1: 총매출
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "INNER JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "    AND SUBSTRING(op.order_date, 1, 6) >= :startMonth " +
        "    AND SUBSTRING(op.order_date, 1, 6) <= :endMonth " +
        "GROUP BY SUBSTRING(op.order_date, 1, 6) " +
        "ORDER BY SUBSTRING(op.order_date, 1, 6)",
        nativeQuery = true)
    List<Object[]> find12MonthSales(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );

    /**
     * 12개월 주문수 추이 조회
     */
    @Query(value = 
        "SELECT " +
        "    SUBSTRING(op.order_date, 1, 6) AS month, " +    // 0: 월 (YYYYMM)
        "    COUNT(op.order_platform_code) AS total_orders " + // 1: 총주문수
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "INNER JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "    AND SUBSTRING(op.order_date, 1, 6) >= :startMonth " +
        "    AND SUBSTRING(op.order_date, 1, 6) <= :endMonth " +
        "GROUP BY SUBSTRING(op.order_date, 1, 6) " +
        "ORDER BY SUBSTRING(op.order_date, 1, 6)",
        nativeQuery = true)
    List<Object[]> find12MonthOrders(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );

    /**
     * 5개월 플랫폼별 매출 조회 (배달분석용)
     * @param hqCode 본사코드
     * @param brandCode 브랜드코드 (0이면 전체)
     * @param startMonth 시작월 (5개월 전, YYYYMM)
     * @param endMonth 종료월 (현재월, YYYYMM)
     */
    @Query(value = 
        "SELECT " +
        "    SUBSTRING(op.order_date, 1, 6) AS month, " +    // 0: 월 (YYYYMM)
        "    sp.platform, " +                                // 1: 플랫폼 (배민/요기요/쿠팡이츠)
        "    COALESCE(SUM(op.order_amount), 0) AS platform_sales " + // 2: 플랫폼별 매출
        "FROM store_platforms sp " +
        "INNER JOIN customer c ON sp.customer_code = c.customer_code " +
        "INNER JOIN order_platforms op ON sp.store_platform_code = op.store_platform_code " +
        "WHERE sp.hq_code = :hqCode " +
        "    AND sp.is_active = true " +
        "    AND (:brandCode = 0 OR sp.brand_code = :brandCode) " +
        "    AND SUBSTRING(op.order_date, 1, 6) >= :startMonth " +
        "    AND SUBSTRING(op.order_date, 1, 6) <= :endMonth " +
        "GROUP BY SUBSTRING(op.order_date, 1, 6), sp.platform " +
        "ORDER BY SUBSTRING(op.order_date, 1, 6), sp.platform",
        nativeQuery = true)
    List<Object[]> find5MonthPlatformSales(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode,
        @Param("startMonth") String startMonth,
        @Param("endMonth") String endMonth
    );
}