package com.inc.sh.repository;

import com.inc.sh.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    
    /**
     * 거래처코드로 거래처 조회
     */
    Customer findByCustomerCode(Integer customerCode);
    
    /**
     * 거래처코드 존재 여부 확인
     */
    boolean existsByCustomerCode(Integer customerCode);
    
    /**
     * 복합 조건으로 거래처 조회
     * @param brandCode 브랜드코드 (null 가능)
     * @param customerName 거래처명 (LIKE 검색, null 가능)
     * @param closeDtYn 종료여부 (1=종료, 0=활성, null=전체)
     * @param orderBlockYn 주문금지여부 (1=금지, 0=허용, null=전체)
     * @return 조회된 거래처 목록
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:brandCode IS NULL OR c.brandCode = :brandCode) AND " +
           "(:customerName IS NULL OR c.customerName LIKE %:customerName%) AND " +
           "(:closeDtYn IS NULL OR " +
           "  (CASE WHEN :closeDtYn = 1 THEN c.closeDt IS NOT NULL " +
           "        WHEN :closeDtYn = 0 THEN c.closeDt IS NULL END)) AND " +
           "(:orderBlockYn IS NULL OR c.orderBlockYn = :orderBlockYn) " +
           "ORDER BY c.customerCode DESC")
    List<Customer> findBySearchConditions(
        @Param("brandCode") Integer brandCode,
        @Param("customerName") String customerName,
        @Param("closeDtYn") Integer closeDtYn,
        @Param("orderBlockYn") Integer orderBlockYn
    );
    
    /**
     * 브랜드별 거래처 조회
     */
    List<Customer> findByBrandCode(Integer brandCode);
    
    /**
     * 활성 거래처 조회 (종료일자가 없는 것)
     */
    @Query("SELECT c FROM Customer c WHERE c.closeDt IS NULL ORDER BY c.customerCode DESC")
    List<Customer> findActiveCustomers();
    
    /**
     * 주문제한 설정용 활성 거래처 조회 (브랜드명 포함)
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.closeDt IS NULL " + // 활성 거래처만
           "ORDER BY c.customerCode ASC")
    List<Object[]> findActiveCustomersWithBrandForOrderLimit();
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함) - 전체가능
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findAllActiveCustomersWithBrand();
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함) - 선택불가 (제한 거래처 제외)
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.customerCode NOT IN :limitedCustomerCodes " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findOrderableCustomersWithBrandExcluding(@Param("limitedCustomerCodes") List<Integer> limitedCustomerCodes);
    
    /**
     * 주문 가능한 거래처 조회 (품목의 주문가능여부와 제한 거래처 고려)
     * @param itemCode 품목코드
     * @param orderAvailableYn 품목의 주문가능여부
     * @param limitedCustomerCodes 제한된 거래처 코드 목록 (order_available_yn=2일 때)
     * @return 주문 가능한 거래처 목록
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "(:orderAvailableYn = 1) OR " + // 전체가능인 경우 모든 거래처
           "(:orderAvailableYn = 2 AND c.customerCode NOT IN :limitedCustomerCodes) " + // 선택불가인 경우 제한 거래처 제외
           "ORDER BY c.customerCode ASC")
    List<Customer> findOrderableCustomersForItem(
        @Param("itemCode") Integer itemCode,
        @Param("orderAvailableYn") Integer orderAvailableYn,
        @Param("limitedCustomerCodes") List<Integer> limitedCustomerCodes
    );
    
    /**
     * 모든 활성 거래처 조회 (order_available_yn=1일 때 사용)
     */
    @Query("SELECT c FROM Customer c ORDER BY c.customerCode ASC")
    List<Customer> findAllActiveCustomers();
    
    /**
     * 주문금지 거래처 조회
     */
    List<Customer> findByOrderBlockYn(Integer orderBlockYn);
    
    /**
     * 거래처의 가상계좌 정보 업데이트
     */
    @Query("UPDATE Customer c SET c.virtualAccountCode = :virtualAccountCode, c.virtualAccount = :virtualAccountNum, c.virtualBankName = :bankName WHERE c.customerCode = :customerCode")
    @Modifying
    void updateVirtualAccountInfo(@Param("customerCode") Integer customerCode,
    							  @Param("virtualAccountCode") Integer virtualAccountCode,
                                  @Param("virtualAccountNum") String virtualAccountNum,
                                  @Param("bankName") String bankName);
    
    /**
     * 팝업용 거래처 검색
     * @param customerCode 거래처코드 (부분일치, null 가능)
     * @param customerName 거래처명 (부분일치, null 가능)
     * @param brandCode 브랜드코드 (완전일치, null 가능)
     * @return 조회된 거래처 목록
     */
    @Query(value = "SELECT * FROM customer c WHERE " +
           "(:customerCode IS NULL OR CAST(c.customer_code AS CHAR) LIKE CONCAT('%', :customerCode, '%')) AND " +
           "(:customerName IS NULL OR c.customer_name LIKE CONCAT('%', :customerName, '%')) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "ORDER BY c.customer_code ASC", nativeQuery = true)
    List<Customer> findByPopupSearchConditions(
        @Param("customerCode") String customerCode,
        @Param("customerName") String customerName,
        @Param("brandCode") Integer brandCode
    );

    /**
     * 브랜드별 후입금 거래처 조회 (미수잔액 관리용)
     */
    @Query(value = "SELECT " +
           "c.customer_code, " +
           "c.customer_name, " +
           "c.owner_name, " +
           "c.credit_limit, " +
           "c.balance_amt " +
           "FROM customer c " +
           "WHERE c.brand_code = :brandCode " +
           "AND c.deposit_type_code = 0 " +
           "ORDER BY c.customer_code", nativeQuery = true)
    List<Object[]> findCustomerBalanceByBrandCode(@Param("brandCode") Integer brandCode);
}