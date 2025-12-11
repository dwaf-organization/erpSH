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
     * 사업자번호로 거래처 조회 (중복 체크용)
     */
    List<Customer> findByBizNum(String bizNum);
    
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
     * 복합 조건으로 거래처 조회 (브랜드명, 물류센터명 포함 - 새로 추가)
     * @param brandCode 브랜드코드 (null 가능)
     * @param customerName 거래처명 (LIKE 검색, null 가능)
     * @param closeDtYn 종료여부 (1=종료, 0=활성, null=전체)
     * @param orderBlockYn 주문금지여부 (1=금지, 0=허용, null=전체)
     * @return Customer, brandName, distCenterName을 포함한 Object[] 배열
     */
    @Query(value = "SELECT " +
           "c.customer_code, " +           // 0
           "c.hq_code, " +                 // 1
           "c.customer_name, " +           // 2
           "c.owner_name, " +              // 3
           "c.biz_num, " +                 // 4
           "c.zip_code, " +                // 5
           "c.addr, " +                    // 6
           "c.biz_type, " +                // 7
           "c.biz_sector, " +              // 8
           "c.email, " +                   // 9
           "c.tel_num, " +                 // 10
           "c.mobile_num, " +              // 11
           "c.fax_num, " +                 // 12
           "c.tax_invoice_yn, " +          // 13
           "c.tax_invoice_name, " +        // 14
           "c.reg_dt, " +                  // 15
           "c.close_dt, " +                // 16
           "c.print_note, " +              // 17
           "c.bank_name, " +               // 18
           "c.account_holder, " +          // 19
           "c.account_num, " +             // 20
           "c.dist_center_code, " +        // 21
           "c.brand_code, " +              // 22
           "c.delivery_weekday, " +        // 23
           "c.deposit_type_code, " +       // 24
           "c.virtual_account, " +         // 25
           "c.virtual_bank_name, " +       // 26
           "c.balance_amt, " +             // 27
           "c.hq_memo, " +                 // 28
           "c.credit_limit, " +            // 29
           "c.collection_day, " +          // 30
           "c.order_block_yn, " +          // 31
           "c.order_block_reason, " +      // 32
           "c.order_block_dt, " +          // 33
           "c.description, " +             // 34
           "c.created_at, " +              // 35
           "c.updated_at, " +              // 36
           "b.brand_name, " +              // 37 - 브랜드명
           "d.dist_center_name " +         // 38 - 물류센터명
           "FROM customer c " +
           "LEFT JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN dist_center d ON c.dist_center_code = d.dist_center_code " +
           "WHERE (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerName IS NULL OR c.customer_name LIKE CONCAT('%', :customerName, '%')) " +
           "AND (:closeDtYn IS NULL OR " +
           "     (CASE WHEN :closeDtYn = 1 THEN c.close_dt IS NOT NULL " +
           "           WHEN :closeDtYn = 0 THEN c.close_dt IS NULL END)) " +
           "AND (:orderBlockYn IS NULL OR c.order_block_yn = :orderBlockYn) " +
           "ORDER BY c.customer_code DESC", nativeQuery = true)
    List<Object[]> findBySearchConditionsWithJoin(
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
     * 주문제한 설정용 활성 거래처 조회 (브랜드명 포함, 본사별)
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.closeDt IS NULL " + // 활성 거래처만
           "AND c.hqCode = :hqCode " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findActiveCustomersWithBrandForOrderLimitByHqCode(@Param("hqCode") Integer hqCode);
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함) - 전체가능
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findAllActiveCustomersWithBrand();
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함, 본사별) - 전체가능
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.hqCode = :hqCode " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findAllActiveCustomersWithBrandByHqCode(@Param("hqCode") Integer hqCode);
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함) - 선택불가 (제한 거래처 제외)
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.customerCode NOT IN :limitedCustomerCodes " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findOrderableCustomersWithBrandExcluding(@Param("limitedCustomerCodes") List<Integer> limitedCustomerCodes);
    
    /**
     * 주문 가능한 거래처 조회 (브랜드명 포함, 본사별) - 선택불가 (제한 거래처 제외)
     */
    @Query("SELECT c.customerCode, c.customerName, b.brandName " +
           "FROM Customer c LEFT JOIN BrandInfo b ON c.brandCode = b.brandCode " +
           "WHERE c.customerCode NOT IN :limitedCustomerCodes " +
           "AND c.hqCode = :hqCode " +
           "ORDER BY c.customerCode ASC")
    List<Object[]> findOrderableCustomersWithBrandExcludingByHqCode(@Param("limitedCustomerCodes") List<Integer> limitedCustomerCodes, @Param("hqCode") Integer hqCode);
    
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
     * 팝업용 거래처 검색 (기존 메서드 - 조인 없음)
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
     * 팝업용 거래처 검색 (브랜드명, 물류센터명 포함 - hq_code 파라미터 추가)
     * @param hqCode 본사코드 (완전일치)
     * @param customerSearch 거래처코드 또는 거래처명 (부분일치, null 가능)
     * @param brandCode 브랜드코드 (완전일치, null 가능, '전체'면 null 처리)
     * @return Customer, brandName, distCenterName을 포함한 Object[] 배열
     */
    @Query(value = "SELECT " +
           "c.customer_code, " +           // 0
           "c.hq_code, " +                 // 1
           "c.customer_name, " +           // 2
           "c.owner_name, " +              // 3
           "c.biz_num, " +                 // 4
           "c.zip_code, " +                // 5
           "c.addr, " +                    // 6
           "c.biz_type, " +                // 7
           "c.biz_sector, " +              // 8
           "c.email, " +                   // 9
           "c.tel_num, " +                 // 10
           "c.mobile_num, " +              // 11
           "c.fax_num, " +                 // 12
           "c.tax_invoice_yn, " +          // 13
           "c.tax_invoice_name, " +        // 14
           "c.reg_dt, " +                  // 15
           "c.close_dt, " +                // 16
           "c.print_note, " +              // 17
           "c.bank_name, " +               // 18
           "c.account_holder, " +          // 19
           "c.account_num, " +             // 20
           "c.dist_center_code, " +        // 21
           "c.brand_code, " +              // 22
           "c.delivery_weekday, " +        // 23
           "c.deposit_type_code, " +       // 24
           "c.virtual_account, " +         // 25
           "c.virtual_bank_name, " +       // 26
           "c.balance_amt, " +             // 27
           "c.hq_memo, " +                 // 28
           "c.credit_limit, " +            // 29
           "c.collection_day, " +          // 30
           "c.order_block_yn, " +          // 31
           "c.order_block_reason, " +      // 32
           "c.order_block_dt, " +          // 33
           "c.description, " +             // 34
           "c.created_at, " +              // 35
           "c.updated_at, " +              // 36
           "b.brand_name, " +              // 37
           "d.dist_center_name " +         // 38
           "FROM customer c " +
           "LEFT JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN dist_center d ON c.dist_center_code = d.dist_center_code " +
           "WHERE c.hq_code = :hqCode " +
           "AND (:customerSearch IS NULL OR " +
           "     CAST(c.customer_code AS CHAR) LIKE CONCAT('%', :customerSearch, '%') OR " +
           "     c.customer_name LIKE CONCAT('%', :customerSearch, '%')) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "ORDER BY c.customer_code ASC", nativeQuery = true)
    List<Object[]> findByPopupSearchConditionsWithJoin(
        @Param("hqCode") Integer hqCode,
        @Param("customerSearch") String customerSearch,
        @Param("brandCode") Integer brandCode
    );
    
    /**
     * 거래처품목조회 팝업 - 복합 조인 쿼리 (기존 Repository에 추가)
     * customer → warehouse → warehouse_items → item + item_customer_price (선택적)
     */
    @Query(value = "SELECT " +
           "i.item_code, " +                    // 0
           "i.item_name, " +                    // 1
           "i.specification, " +                // 2
           "i.purchase_unit, " +                // 3
           "i.vat_type, " +                     // 4
           "i.vat_detail, " +                   // 5
           "i.origin, " +                       // 6
           "i.category_code, " +                // 7
           "ic.category_name, " +               // 8
           "w.warehouse_code, " +               // 9
           "w.warehouse_name, " +               // 10
           "wi.current_quantity, " +            // 11
           "wi.safe_quantity, " +               // 12
           // 가격 정보 (거래처별 우선, 없으면 기본가격)
           "COALESCE(icp.customer_supply_price, i.base_price) AS base_price, " +     // 13
           "COALESCE(icp.supply_price, i.supply_price) AS supply_price, " +          // 14
           "COALESCE(icp.tax_amount, i.tax_amount) AS tax_amount, " +                // 15
           "COALESCE(icp.taxable_amount, i.taxable_amount) AS taxable_amount, " +    // 16
           "COALESCE(icp.duty_free_amount, i.duty_free_amount) AS duty_free_amount, " + // 17
           "COALESCE(icp.total_amount, i.total_amount) AS total_amount, " +          // 18
           "i.order_available_yn, " +           // 19
           "i.min_order_qty, " +               // 20
           "i.max_order_qty, " +               // 21
           "i.deadline_day, " +                 // 22
           "i.deadline_time, " +                 // 23
           "i.price_type " +                 // 24
           "FROM customer c " +
           "INNER JOIN warehouse w ON c.dist_center_code = w.dist_center_code " +
           "INNER JOIN warehouse_items wi ON w.warehouse_code = wi.warehouse_code " +
           "INNER JOIN item i ON wi.item_code = i.item_code " +
           "LEFT JOIN item_category ic ON i.category_code = ic.category_code " +
           "LEFT JOIN item_customer_price icp ON (i.item_code = icp.item_code AND c.customer_code = icp.customer_code " +
           "         AND (icp.end_dt IS NULL OR icp.end_dt >= CURDATE())) " +
           "WHERE c.hq_code = :hqCode " +
           "AND c.customer_code = :customerCode " +
           "AND (:warehouseCode IS NULL OR w.warehouse_code = :warehouseCode) " +
           "AND (:categoryCode IS NULL OR i.category_code = :categoryCode) " +
           "AND (:priceType IS NULL OR i.price_type = :priceType) " +
           "AND (:item IS NULL OR " +
           "     CAST(i.item_code AS CHAR) LIKE CONCAT('%', :item, '%') OR " +
           "     i.item_name LIKE CONCAT('%', :item, '%')) " +
           "AND i.order_available_yn = 1 " +    // 주문가능 품목만
           "AND w.use_yn = 1 " +                // 사용중 창고만
           "ORDER BY i.item_code ASC", nativeQuery = true)
    List<Object[]> findCustomerItemsWithPrice(
        @Param("hqCode") Integer hqCode,
        @Param("customerCode") Integer customerCode,
        @Param("item") String item,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("priceType") Integer priceType
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

    /**
     * 브랜드별 후입금 거래처 미수잔액 조회 (본사별)
     */
    @Query(value = "SELECT " +
           "c.customer_code, " +
           "c.customer_name, " +
           "c.owner_name, " +
           "c.credit_limit, " +
           "c.balance_amt " +
           "FROM customer c " +
           "WHERE (:brandCode IS NULL OR c.brand_code = :brandCode) " +
//           "AND c.deposit_type_code = 0 " +
           "AND c.hq_code = :hqCode " +
           "ORDER BY c.customer_code", nativeQuery = true)
    List<Object[]> findCustomerBalanceByBrandCodeWithHqCode(
        @Param("brandCode") Integer brandCode,
        @Param("hqCode") Integer hqCode
    );
    

    /**
     * 거래처코드로 조회 (brandName, distCenterName 조인 포함) - 저장 후 응답용
     */
    @Query(value = "SELECT " +
           "c.customer_code, " +           // 0
           "c.hq_code, " +                 // 1
           "c.customer_name, " +           // 2
           "c.owner_name, " +              // 3
           "c.biz_num, " +                 // 4
           "c.zip_code, " +                // 5
           "c.addr, " +                    // 6
           "c.biz_type, " +                // 7
           "c.biz_sector, " +              // 8
           "c.email, " +                   // 9
           "c.tel_num, " +                 // 10
           "c.mobile_num, " +              // 11
           "c.fax_num, " +                 // 12
           "c.tax_invoice_yn, " +          // 13
           "c.tax_invoice_name, " +        // 14
           "c.reg_dt, " +                  // 15
           "c.close_dt, " +                // 16
           "c.print_note, " +              // 17
           "c.bank_name, " +               // 18
           "c.account_holder, " +          // 19
           "c.account_num, " +             // 20
           "c.dist_center_code, " +        // 21
           "c.brand_code, " +              // 22
           "c.delivery_weekday, " +        // 23
           "c.deposit_type_code, " +       // 24
           "c.virtual_account, " +         // 25
           "c.virtual_bank_name, " +       // 26
           "c.balance_amt, " +             // 27
           "c.hq_memo, " +                 // 28
           "c.credit_limit, " +            // 29
           "c.collection_day, " +          // 30
           "c.order_block_yn, " +          // 31
           "c.order_block_reason, " +      // 32
           "c.order_block_dt, " +          // 33
           "c.description, " +             // 34
           "c.created_at, " +              // 35
           "c.updated_at, " +              // 36
           "b.brand_name, " +              // 37
           "d.dist_center_name " +         // 38
           "FROM customer c " +
           "LEFT JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN dist_center d ON c.dist_center_code = d.dist_center_code " +
           "WHERE c.customer_code = :customerCode", nativeQuery = true)
    List<Object[]> findCustomerWithJoinByCustomerCode(@Param("customerCode") Integer customerCode);
}