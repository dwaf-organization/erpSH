package com.inc.sh.repository;

import com.inc.sh.entity.OrderPlatforms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderPlatformsRepository extends JpaRepository<OrderPlatforms, Integer> {
    
    /**
     * 주문번호로 조회 (중복 확인용)
     */
    Optional<OrderPlatforms> findByOrderNo(String orderNo);
    
    /**
     * 플랫폼별 주문 조회
     */
    List<OrderPlatforms> findByPlatform(String platform);
    
    /**
     * 거래처별 주문 조회
     */
    List<OrderPlatforms> findByCustomerCode(Integer customerCode);
    
    /**
     * 매장플랫폼별 주문 조회
     */
    List<OrderPlatforms> findByStorePlatformCode(Integer storePlatformCode);
    
    /**
     * 기간별 주문 조회
     */
    List<OrderPlatforms> findByOrderDateBetween(String startDate, String endDate);
    
    /**
     * 플랫폼 + 기간별 주문 조회
     */
    List<OrderPlatforms> findByPlatformAndOrderDateBetween(String platform, String startDate, String endDate);
    
    /**
     * 하이픈 거래번호로 조회 (중복 확인용)
     */
    Optional<OrderPlatforms> findByHyphenTrNo(String hyphenTrNo);
    
    /**
     * 주문번호 존재 여부 확인
     */
    boolean existsByOrderNo(String orderNo);
    
    /**
     * 일별 매출 집계 데이터 조회 (플랫폼별, 날짜별)
     */
    @Query("SELECT COUNT(op), COALESCE(SUM(op.orderAmount), 0) " +
           "FROM OrderPlatforms op " +
           "WHERE op.platform = :platform " +
           "AND op.orderDate = :orderDate " +
           "AND op.brandCode = :brandCode")
    Object[] findDailySummaryByPlatformAndDate(
            @Param("platform") String platform, 
            @Param("orderDate") String orderDate,
            @Param("brandCode") Integer brandCode);
    
    /**
     * 매출액 기준 매장 순위 조회 (최근 7일)
     */
    @Query("SELECT op.storePlatformCode, op.customerCode, sp.platformStoreName, " +
           "COALESCE(SUM(op.orderAmount), 0) as totalAmount " +
           "FROM OrderPlatforms op " +
           "JOIN StorePlatforms sp ON op.storePlatformCode = sp.storePlatformCode " +
           "WHERE op.platform = :platform " +
           "AND op.orderDate >= :startDate " +
           "AND op.brandCode = :brandCode " +
           "GROUP BY op.storePlatformCode, op.customerCode, sp.platformStoreName " +
           "ORDER BY totalAmount DESC")
    List<Object[]> findStoreRankingByAmount(
            @Param("platform") String platform, 
            @Param("startDate") String startDate,
            @Param("brandCode") Integer brandCode);
    
    /**
     * 주문수 기준 매장 순위 조회 (최근 7일)
     */
    @Query("SELECT op.storePlatformCode, op.customerCode, sp.platformStoreName, " +
           "COUNT(op) as orderCount " +
           "FROM OrderPlatforms op " +
           "JOIN StorePlatforms sp ON op.storePlatformCode = sp.storePlatformCode " +
           "WHERE op.platform = :platform " +
           "AND op.orderDate >= :startDate " +
           "AND op.brandCode = :brandCode " +
           "GROUP BY op.storePlatformCode, op.customerCode, sp.platformStoreName " +
           "ORDER BY orderCount DESC")
    List<Object[]> findStoreRankingByOrderCount(
            @Param("platform") String platform, 
            @Param("startDate") String startDate,
            @Param("brandCode") Integer brandCode);
}