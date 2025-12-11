package com.inc.sh.repository;

import com.inc.sh.entity.CustomerAccountTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerAccountTransactionsRepository extends JpaRepository<CustomerAccountTransactions, Integer> {
    
    /**
     * 거래처별 계좌 거래 내역 조회
     */
    List<CustomerAccountTransactions> findByCustomerCodeOrderByTransactionDateDesc(Integer customerCode);
    
    /**
     * 가상계좌별 거래 내역 조회
     */
    List<CustomerAccountTransactions> findByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 거래유형별 조회
     */
    List<CustomerAccountTransactions> findByTransactionType(String transactionType);
    
    /**
     * 참조 ID별 조회 (주문번호 등)
     */
    List<CustomerAccountTransactions> findByReferenceId(String referenceId);
    
    /**
     * 거래처의 최신 잔액 조회
     */
    @Query("SELECT cat.balanceAfter FROM CustomerAccountTransactions cat " +
           "WHERE cat.customerCode = :customerCode " +
           "ORDER BY cat.transactionDate DESC, cat.transactionCode DESC " +
           "LIMIT 1")
    Integer findLatestBalanceByCustomerCode(@Param("customerCode") Integer customerCode);

    /**
     * 참조ID와 참조유형으로 거래내역 조회 (입금 삭제 시 사용)
     */
    List<CustomerAccountTransactions> findByReferenceIdAndReferenceType(@Param("referenceId") String referenceId, @Param("referenceType") String referenceType);
    
    /**
     * 참조유형 및 참조ID로 거래내역 조회 (주문 결제내역 찾기용 - OrderService용)
     */
    CustomerAccountTransactions findByReferenceTypeAndReferenceId(
        @Param("referenceType") String referenceType,
        @Param("referenceId") String referenceId
    );
    
    /**
     * 참조ID와 참조유형으로 거래내역 삭제 (입금 삭제 시 사용)
     */
    @Modifying
    void deleteByReferenceIdAndReferenceType(@Param("referenceId") String referenceId, @Param("referenceType") String referenceType);

    /**
     * 거래처조정처리 조회 (거래처명 포함, hqCode 조건 추가, 날짜 범위 검색)
     */
    @Query(value = "SELECT " +
           "cat.transaction_code, " +
           "cat.customer_code, " +
           "c.customer_name, " +
           "cat.transaction_date, " +
           "cat.amount, " +
           "cat.note " +
           "FROM customer_account_transactions cat " +
           "JOIN customer c ON cat.customer_code = c.customer_code " +
           "WHERE cat.transaction_type = '조정' " +
           "AND c.hq_code = :hqCode " +
           "AND (:customerCode IS NULL OR cat.customer_code = :customerCode) " +
           "AND (:adjustmentDateStart IS NULL OR cat.transaction_date >= :adjustmentDateStart) " +
           "AND (:adjustmentDateEnd IS NULL OR cat.transaction_date <= :adjustmentDateEnd) " +
           "ORDER BY cat.transaction_date DESC, cat.transaction_code DESC", nativeQuery = true)
    List<Object[]> findCustomerAdjustmentsWithDateRange(
        @Param("hqCode") Integer hqCode,
        @Param("customerCode") Integer customerCode,
        @Param("adjustmentDateStart") String adjustmentDateStart,
        @Param("adjustmentDateEnd") String adjustmentDateEnd
    );

    /**
     * 거래처별 전일잔액 조회 (시작일 이전 최근 잔액)
     */
    @Query(value = "SELECT customer_code, balance_after " +
           "FROM customer_account_transactions " +
           "WHERE customer_code = :customerCode " +
           "AND transaction_date < :startDate " +
           "ORDER BY transaction_date DESC, transaction_code DESC " +
           "LIMIT 1", nativeQuery = true)
    Object[] findPreviousBalanceByCustomerCode(
        @Param("customerCode") Integer customerCode,
        @Param("startDate") String startDate
    );

    /**
     * 거래처별 기간 내 거래 집계 조회
     */
    @Query(value = "SELECT " +
           "customer_code, " +
           "SUM(CASE WHEN transaction_type IN ('출금', '외상') THEN ABS(amount) ELSE 0 END) as sales_amount, " +
           "SUM(CASE WHEN transaction_type = '반품입금' THEN amount ELSE 0 END) as return_amount, " +
           "SUM(CASE WHEN transaction_type = '입금' THEN amount ELSE 0 END) as deposit_amount, " +
           "SUM(CASE WHEN transaction_type = '조정' THEN amount ELSE 0 END) as adjustment_amount " +
           "FROM customer_account_transactions " +
           "WHERE customer_code = :customerCode " +
           "AND transaction_date >= :startDate " +
           "AND transaction_date <= :endDate " +
           "GROUP BY customer_code", nativeQuery = true)
    Object[] findTransactionSummaryByCustomerCode(
        @Param("customerCode") Integer customerCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );

    /**
     * 전체 거래처별 기간 내 거래 집계 조회
     */
    @Query(value = "SELECT " +
           "cat.customer_code, " +
           "c.customer_name, " +
           "c.credit_limit, " +
           "SUM(CASE WHEN cat.transaction_type IN ('출금', '외상') THEN ABS(cat.amount) ELSE 0 END) as sales_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '반품입금' THEN cat.amount ELSE 0 END) as return_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '입금' THEN cat.amount ELSE 0 END) as deposit_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '조정' THEN cat.amount ELSE 0 END) as adjustment_amount " +
           "FROM customer_account_transactions cat " +
           "JOIN customer c ON cat.customer_code = c.customer_code " +
           "WHERE cat.transaction_date >= :startDate " +
           "AND cat.transaction_date <= :endDate " +
           "AND (:customerCode IS NULL OR cat.customer_code = :customerCode) " +
           "GROUP BY cat.customer_code, c.customer_name, c.credit_limit " +
           "ORDER BY cat.customer_code", nativeQuery = true)
    List<Object[]> findAllCustomerTransactionSummary(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode
    );

    /**
     * 전체 거래처별 기간 내 거래 집계 조회 (본사별)
     */
    @Query(value = "SELECT " +
           "cat.customer_code, " +
           "c.customer_name, " +
           "c.credit_limit, " +
           "SUM(CASE WHEN cat.transaction_type IN ('출금', '외상') THEN ABS(cat.amount) ELSE 0 END) as sales_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '반품입금' THEN cat.amount ELSE 0 END) as return_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '입금' THEN cat.amount ELSE 0 END) as deposit_amount, " +
           "SUM(CASE WHEN cat.transaction_type = '조정' THEN cat.amount ELSE 0 END) as adjustment_amount " +
           "FROM customer_account_transactions cat " +
           "JOIN customer c ON cat.customer_code = c.customer_code " +
           "WHERE cat.transaction_date >= :startDate " +
           "AND cat.transaction_date <= :endDate " +
           "AND (:customerCode IS NULL OR cat.customer_code = :customerCode) " +
           "AND c.hq_code = :hqCode " +
           "GROUP BY cat.customer_code, c.customer_name, c.credit_limit " +
           "ORDER BY cat.customer_code", nativeQuery = true)
    List<Object[]> findAllCustomerTransactionSummaryWithHqCode(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("hqCode") Integer hqCode
    );
}