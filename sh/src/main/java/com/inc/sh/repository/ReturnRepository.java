package com.inc.sh.repository;

import com.inc.sh.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, String> {
    
    /**
     * 반품번호로 조회
     */
    Return findByReturnNo(String returnNo);
    
    /**
     * 거래처별 반품 조회
     */
    List<Return> findByReturnCustomerCode(Integer returnCustomerCode);
    
    /**
     * 품목별 반품 조회
     */
    List<Return> findByItemCode(Integer itemCode);
    
    /**
     * 진행상태별 반품 조회
     */
    List<Return> findByProgressStatus(String progressStatus);

    /**
     * 거래처별원장용 반품 집계 조회
     */
    @Query(value = "SELECT " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "0 as tax_free_amt, " +  // 반품은 모두 과세로 가정
           "SUM(r.supply_price) as taxable_amt, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) " +
           "GROUP BY r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num " +
           "ORDER BY r.return_customer_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnSummary(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 반품 집계 조회 (본사별)
     */
    @Query(value = "SELECT " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "0 as tax_free_amt, " +
           "SUM(r.supply_price) as taxable_amt, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN `order` o ON r.order_no = o.order_no " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) AND " +
           "o.hq_code = :hqCode " +
           "GROUP BY r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num " +
           "ORDER BY r.return_customer_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnSummaryWithHqCode(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 거래처별원장용 반품 세부 조회
     */
    @Query(value = "SELECT " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "r.item_code, " +
           "r.item_name, " +
           "r.specification, " +
           "r.unit, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) " +
           "GROUP BY r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num, r.item_code, r.item_name, r.specification, r.unit " +
           "ORDER BY r.return_customer_code, r.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnDetail(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 반품 세부 조회 (본사별)
     */
    @Query(value = "SELECT " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "r.item_code, " +
           "r.item_name, " +
           "r.specification, " +
           "r.unit, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN `order` o ON r.order_no = o.order_no " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) AND " +
           "o.hq_code = :hqCode " +
           "GROUP BY r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num, r.item_code, r.item_name, r.specification, r.unit " +
           "ORDER BY r.return_customer_code, r.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnDetailWithHqCode(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 거래처별원장용 반품 일자별 조회
     */
    @Query(value = "SELECT " +
           "r.return_request_dt as order_date, " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "r.item_code, " +
           "r.item_name, " +
           "r.specification, " +
           "r.unit, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) " +
           "GROUP BY r.return_request_dt, r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num, r.item_code, r.item_name, r.specification, r.unit " +
           "ORDER BY r.return_request_dt, r.return_customer_code, r.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnDaily(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 반품 일자별 조회 (본사별)
     */
    @Query(value = "SELECT " +
           "r.return_request_dt as order_date, " +
           "r.return_customer_code as customer_code, " +
           "r.return_customer_name as customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "r.item_code, " +
           "r.item_name, " +
           "r.specification, " +
           "r.unit, " +
           "'반품' as order_type, " +
           "SUM(r.qty) as total_qty, " +
           "SUM(r.supply_price) as supply_amt, " +
           "SUM(r.vat_amt) as vat_amt, " +
           "SUM(r.total_amt) as total_amt, " +
           "SUM(r.qty) as delivery_qty, " +
           "SUM(r.supply_price) as delivery_supply_amt, " +
           "SUM(r.vat_amt) as delivery_vat_amt, " +
           "SUM(r.total_amt) as delivery_total_amt " +
           "FROM `return` r " +
           "JOIN customer c ON r.return_customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "LEFT JOIN `order` o ON r.order_no = o.order_no " +
           "WHERE " +
           "(:deliveryRequestDtStart IS NULL OR r.return_request_dt >= :deliveryRequestDtStart) AND " +
           "(:deliveryRequestDtEnd IS NULL OR r.return_request_dt <= :deliveryRequestDtEnd) AND " +
           "(:itemCode IS NULL OR r.item_code = :itemCode) AND " +
           "(:brandCode IS NULL OR c.brand_code = :brandCode) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) AND " +
           "o.hq_code = :hqCode " +
           "GROUP BY r.return_request_dt, r.return_customer_code, r.return_customer_name, b.brand_name, c.tel_num, r.item_code, r.item_name, r.specification, r.unit " +
           "ORDER BY r.return_request_dt, r.return_customer_code, r.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerReturnDailyWithHqCode(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 반품등록처리 - 검색 조건으로 반품 목록 조회 (거래처명, 창고명, 물류센터명 JOIN)
     */
    @Query(value = "SELECT " +
           "r.return_no, " +                      // 0
           "r.return_customer_code, " +           // 1
           "c.customer_name, " +                  // 2
           "r.return_request_dt, " +              // 3
           "r.item_code, " +                      // 4
           "r.item_name, " +                      // 5
           "r.specification, " +                  // 6
           "r.unit, " +                           // 7
           "r.price_type, " +                     // 8
           "r.qty, " +                            // 9
           "r.unit_price, " +                     // 10
           "r.supply_price, " +                   // 11
           "r.vat_amt, " +                        // 12
           "r.total_amt, " +                      // 13
           "r.receive_warehouse_code, " +         // 14
           "w.warehouse_name, " +                 // 15
           "dc.dist_center_code, " +              // 16
           "dc.dist_center_name, " +              // 17
           "r.progress_status, " +                // 18
           "r.return_approve_dt, " +              // 19
           "r.reply_message, " +                  // 20
           "r.note, " +                           // 21
           "r.return_message, " +                 // 22
           "r.warehouse_name as stored_warehouse_name, " +  // 23
           "r.order_no as order_no " +            // 24
           "FROM `return` r " +
           "LEFT JOIN customer c ON r.return_customer_code = c.customer_code " +
           "LEFT JOIN warehouse w ON r.receive_warehouse_code = w.warehouse_code " +
           "LEFT JOIN dist_center dc ON w.dist_center_code = dc.dist_center_code " +
           "WHERE " +
           "(:startDate IS NULL OR r.return_request_dt >= :startDate) AND " +
           "(:endDate IS NULL OR r.return_request_dt <= :endDate) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) AND " +
           "(:status IS NULL OR r.progress_status = :status) " +
           "ORDER BY r.return_request_dt DESC, r.return_no DESC", nativeQuery = true)
    List<Object[]> findReturnsWithJoinByConditions(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("status") String status
    );
    
    /**
     * 반품등록처리 - 검색 조건으로 반품 목록 조회 (본사별, order 테이블 조인)
     */
    @Query(value = "SELECT " +
           "r.return_no, " +                      // 0
           "r.return_customer_code, " +           // 1
           "c.customer_name, " +                  // 2
           "r.return_request_dt, " +              // 3
           "r.item_code, " +                      // 4
           "r.item_name, " +                      // 5
           "r.specification, " +                  // 6
           "r.unit, " +                           // 7
           "r.price_type, " +                     // 8
           "r.qty, " +                            // 9
           "r.unit_price, " +                     // 10
           "r.supply_price, " +                   // 11
           "r.vat_amt, " +                        // 12
           "r.total_amt, " +                      // 13
           "r.receive_warehouse_code, " +         // 14
           "w.warehouse_name, " +                 // 15
           "dc.dist_center_code, " +              // 16
           "dc.dist_center_name, " +              // 17
           "r.progress_status, " +                // 18
           "r.return_approve_dt, " +              // 19
           "r.reply_message, " +                  // 20
           "r.note, " +                           // 21
           "r.return_message, " +                 // 22
           "r.warehouse_name as stored_warehouse_name, " +  // 23
           "r.order_no as order_no, " +            // 24
           "r.unit_price, " +                      // 25
           "r.order_item_code, " +                 // 26 - 주문품목코드 추가
           "oi.order_qty, " +                      // 27 - 주문수량 추가
           "(oi.order_qty - oi.returned_qty) as available_return_qty " + // 28 - 반품가능수량 추가
           "FROM `return` r " +
           "LEFT JOIN customer c ON r.return_customer_code = c.customer_code " +
           "LEFT JOIN warehouse w ON r.receive_warehouse_code = w.warehouse_code " +
           "LEFT JOIN dist_center dc ON w.dist_center_code = dc.dist_center_code " +
           "LEFT JOIN `order` o ON r.order_no = o.order_no " +  // order 테이블 조인
           "LEFT JOIN order_item oi ON r.order_item_code = oi.order_item_code " +
           "WHERE " +
           "(:startDate IS NULL OR r.return_request_dt >= :startDate) AND " +
           "(:endDate IS NULL OR r.return_request_dt <= :endDate) AND " +
           "(:customerCode IS NULL OR r.return_customer_code = :customerCode) AND " +
           "(:status IS NULL OR :status = '' OR r.progress_status = :status) AND " +
           "o.hq_code = :hqCode " +               // 본사코드 필터링
           "ORDER BY r.return_request_dt DESC, r.return_no DESC", nativeQuery = true)
    List<Object[]> findReturnsWithJoinByConditionsAndHqCode(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("status") String status,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 반품등록처리 - 미승인 상태의 반품만 삭제 가능한지 확인
     */
    @Query("SELECT r FROM Return r WHERE r.returnNo = :returnNo AND r.progressStatus = '미승인'")
    Return findByReturnNoAndStatusUnapproved(@Param("returnNo") String returnNo);
    
    /**
     * 반품번호 생성용 - 날짜별 최신 반품번호 조회
     */
    @Query(value = "SELECT return_no FROM `return` " +
           "WHERE return_no LIKE CONCAT(:datePrefix, '%') " +
           "ORDER BY return_no DESC LIMIT 1", nativeQuery = true)
    String findLatestReturnNoByDate(@Param("datePrefix") String datePrefix);
    
    /**
     * [앱전용] 최근 반품 요청 금액 조회 (return_request_dt 기준 최근 1건)
     */
    @Query(value = "SELECT r.total_amt " +
           "FROM `return` r " +
           "WHERE r.return_customer_code = :customerCode " +
           "ORDER BY r.return_request_dt DESC " +
           "LIMIT 1", nativeQuery = true)
    Integer findRecentReturnAmountByCustomerCode(@Param("customerCode") Integer customerCode);

    /**
     * 주문품목별 총 반품수량 조회
     */
    @Query("SELECT COALESCE(SUM(r.qty), 0) FROM Return r WHERE r.orderItemCode = :orderItemCode")
    Integer getTotalReturnedQtyByOrderItemCode(@Param("orderItemCode") Integer orderItemCode);

    /**
     * 주문번호별 반품 목록 조회
     */
    List<Return> findByOrderNoOrderByCreatedAtDesc(String orderNo);

    /**
     * 주문품목별 반품 목록 조회
     */
    List<Return> findByOrderItemCodeOrderByCreatedAtDesc(Integer orderItemCode);
}