package com.inc.sh.repository;

import com.inc.sh.entity.Deposits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositsRepository extends JpaRepository<Deposits, Integer> {
    
    /**
     * 거래처별 입금 내역 조회
     */
    List<Deposits> findByCustomerCode(Integer customerCode);
    
    /**
     * 가상계좌별 입금 내역 조회
     */
    List<Deposits> findByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 입금일자별 조회
     */
    @Query("SELECT d FROM Deposits d WHERE d.depositDate BETWEEN :startDate AND :endDate ORDER BY d.depositDate DESC")
    List<Deposits> findByDepositDateBetween(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 거래처수금처리 조회 (거래처명 포함)
     */
    @Query(value = "SELECT " +
           "d.deposit_id, " +
           "d.customer_code, " +
           "c.customer_name, " +
           "d.deposit_date, " +
           "d.deposit_amount, " +
           "d.deposit_method, " +
           "d.depositor_name, " +
           "d.note " +
           "FROM deposits d " +
           "JOIN customer c ON d.customer_code = c.customer_code " +
           "WHERE (:customerCode IS NULL OR d.customer_code = :customerCode) " +
           "AND d.deposit_date >= :startDate " +
           "AND d.deposit_date <= :endDate " +
           "AND (:depositMethod IS NULL OR " +
           "  (CASE WHEN :depositMethod = 0 THEN c.deposit_type_code = 0 " +
           "        WHEN :depositMethod = 1 THEN c.deposit_type_code = 1 END)) " +
           "ORDER BY d.deposit_date DESC, d.deposit_id DESC", nativeQuery = true)
    List<Object[]> findCustomerDepositsWithConditions(
        @Param("customerCode") Integer customerCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("depositMethod") Integer depositMethod
    );

    /**
     * 거래처수금처리 조회 (거래처명 포함, 본사별)
     */
    @Query(value = "SELECT " +
           "d.deposit_id, " +
           "d.customer_code, " +
           "c.customer_name, " +
           "d.deposit_date, " +
           "d.deposit_amount, " +
           "d.deposit_method, " +
           "d.depositor_name, " +
           "d.note " +
           "FROM deposits d " +
           "JOIN customer c ON d.customer_code = c.customer_code " +
           "WHERE (:customerCode IS NULL OR d.customer_code = :customerCode) " +
           "AND d.deposit_date >= :startDate " +
           "AND d.deposit_date <= :endDate " +
           "AND (:depositMethod IS NULL OR " +
           "  (CASE WHEN :depositMethod = 0 THEN c.deposit_type_code = 0 " +
           "        WHEN :depositMethod = 1 THEN c.deposit_type_code = 1 END)) " +
           "AND c.hq_code = :hqCode " +
           "ORDER BY d.deposit_date DESC, d.deposit_id DESC", nativeQuery = true)
    List<Object[]> findCustomerDepositsWithConditionsWithHqCode(
        @Param("customerCode") Integer customerCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("depositMethod") Integer depositMethod,
        @Param("hqCode") Integer hqCode
    );

    /**
     * 거래처별수금현황 조회 (브랜드 포함)
     */
    @Query(value = "SELECT " +
           "d.deposit_id, " +
           "d.deposit_date, " +
           "d.customer_code, " +
           "c.customer_name, " +
           "d.deposit_amount, " +
           "d.deposit_method, " +
           "d.note " +
           "FROM deposits d " +
           "JOIN customer c ON d.customer_code = c.customer_code " +
           "WHERE d.deposit_date >= :startDate " +
           "AND d.deposit_date <= :endDate " +
           "AND (:customerCode IS NULL OR d.customer_code = :customerCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:depositMethod IS NULL OR d.deposit_method = :depositMethod) " +
           "ORDER BY d.deposit_date DESC, d.deposit_id DESC", nativeQuery = true)
    List<Object[]> findCustomerDepositStatusWithConditions(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("brandCode") Integer brandCode,
        @Param("depositMethod") String depositMethod
    );

    /**
     * 거래처별수금현황 조회 (브랜드 포함, 본사별)
     */
    @Query(value = "SELECT " +
           "d.deposit_id, " +
           "d.deposit_date, " +
           "d.customer_code, " +
           "c.customer_name, " +
           "d.deposit_amount, " +
           "d.deposit_method, " +
           "d.note " +
           "FROM deposits d " +
           "JOIN customer c ON d.customer_code = c.customer_code " +
           "WHERE d.deposit_date >= :startDate " +
           "AND d.deposit_date <= :endDate " +
           "AND (:customerCode IS NULL OR d.customer_code = :customerCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:depositMethod IS NULL OR d.deposit_method = :depositMethod) " +
           "AND c.hq_code = :hqCode " +
           "ORDER BY d.deposit_date DESC, d.deposit_id DESC", nativeQuery = true)
    List<Object[]> findCustomerDepositStatusWithConditionsWithHqCode(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("brandCode") Integer brandCode,
        @Param("depositMethod") Integer depositMethod,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * [앱전용] 최근 입금 금액 조회 (deposit_date 기준 최근 1건)
     */
    @Query(value = "SELECT d.deposit_amount " +
           "FROM deposits d " +
           "WHERE d.customer_code = :customerCode " +
           "ORDER BY d.deposit_date DESC " +
           "LIMIT 1", nativeQuery = true)
    Integer findRecentDepositAmountByCustomerCode(@Param("customerCode") Integer customerCode);
}