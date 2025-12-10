package com.inc.sh.repository;

import com.inc.sh.entity.Order;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    
    /**
     * 차량의 배송중인 주문 조회 (차량 삭제시 확인용)
     * 배송상태가 '배송요청' 또는 '배송중'인 주문
     */
    @Query("SELECT o.orderNo FROM Order o WHERE o.vehicleCode = :vehicleCode " +
           "AND o.deliveryStatus IN ('배송요청', '배송중')")
    List<String> findActiveOrdersByVehicleCode(@Param("vehicleCode") Integer vehicleCode);
    
    /**
     * 차량의 배송중인 주문 존재 여부 확인
     */
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.vehicleCode = :vehicleCode " +
           "AND o.deliveryStatus IN ('배송요청', '배송중')")
    boolean existsActiveOrdersByVehicleCode(@Param("vehicleCode") Integer vehicleCode);
    
    /**
     * 차량별 주문 조회
     */
    List<Order> findByVehicleCode(Integer vehicleCode);
    
    /**
     * 주문번호로 조회
     */
    Order findByOrderNo(String orderNo);
    
    /**
     * 주문번호 존재 여부 확인
     */
    boolean existsByOrderNo(String orderNo);
    
    /**
     * 주문 목록 조회 (검색 조건)
     * @param orderDtStart 주문일자 시작
     * @param orderDtEnd 주문일자 끝  
     * @param customerName 거래처명 (부분일치)
     * @param deliveryStatus 배송상태
     * @return 조회된 주문 목록
     */
    @Query(value = "SELECT o.* FROM `order` o " +
           "WHERE " +
           "(:orderDtStart IS NULL OR o.order_dt >= :orderDtStart) AND " +
           "(:orderDtEnd IS NULL OR o.order_dt <= :orderDtEnd) AND " +
           "(:customerName IS NULL OR o.customer_name LIKE CONCAT('%', :customerName, '%')) AND " +
           "(:deliveryStatus IS NULL OR o.delivery_status = :deliveryStatus) " +
           "ORDER BY o.order_dt DESC, o.order_no DESC", nativeQuery = true)
    List<Order> findBySearchConditions(
        @Param("orderDtStart") String orderDtStart,
        @Param("orderDtEnd") String orderDtEnd,
        @Param("customerName") String customerName,
        @Param("deliveryStatus") String deliveryStatus
    );
    
    /**
     * 주문 조회 (본사별)
     */
    @Query(value = "SELECT o.* FROM `order` o " +
           "WHERE " +
           "(:orderDtStart IS NULL OR o.order_dt >= :orderDtStart) AND " +
           "(:orderDtEnd IS NULL OR o.order_dt <= :orderDtEnd) AND " +
           "(:customerName IS NULL OR o.customer_name LIKE CONCAT('%', :customerName, '%')) AND " +
           "(:deliveryStatus IS NULL OR o.delivery_status = :deliveryStatus) AND " +
           "o.hq_code = :hqCode " +
           "ORDER BY o.order_dt DESC, o.order_no DESC", nativeQuery = true)
    List<Order> findBySearchConditionsWithHqCode(
        @Param("orderDtStart") String orderDtStart,
        @Param("orderDtEnd") String orderDtEnd,
        @Param("customerName") String customerName,
        @Param("deliveryStatus") String deliveryStatus,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 오늘 날짜의 최신 주문번호 조회 (순번 생성용)
     */
    @Query(value = "SELECT o.order_no FROM `order` o " +
           "WHERE o.order_no LIKE CONCAT(:datePrefix, '%') " +
           "ORDER BY o.order_no DESC LIMIT 1", nativeQuery = true)
    String findLatestOrderNoByDate(@Param("datePrefix") String datePrefix);
    
    /**
     * 반품등록용 - 거래처별 최근 30일 배송완료 주문번호 조회 (셀렉트박스용)
     */
    @Query(value = "SELECT DISTINCT o.order_no " +
           "FROM `order` o " +
           "WHERE o.customer_code = :customerCode " +
           "AND o.delivery_status = '배송완료' " +
           "AND o.order_dt >= :thirtyDaysAgo " +
           "ORDER BY o.order_no DESC", nativeQuery = true)
    List<String> findRecentCompletedOrdersByCustomer(
        @Param("customerCode") Integer customerCode,
        @Param("thirtyDaysAgo") String thirtyDaysAgo
    );
    
    /**
     * 물류대금마감현황 - 주문 및 거래처 정보 조회 (회수기일 계산 포함)
     */
    @Query(value = "SELECT " +
           "o.order_no, " +                                    // 0
           "o.customer_code, " +                               // 1
           "c.customer_name, " +                               // 2
           "o.order_dt, " +                                    // 3
           "c.collection_day, " +                              // 4
           "o.payment_at, " +                                  // 5 (납부일자)
           "o.supply_amt, " +                                  // 6
           "o.vat_amt, " +                                     // 7
           "o.total_amt, " +                                   // 8
           "o.payment_status, " +                              // 9
           "DATE_FORMAT(DATE_ADD(STR_TO_DATE(o.order_dt, '%Y%m%d'), INTERVAL c.collection_day DAY), '%Y%m%d') as collection_due_date " + // 10 (계산된 회수기일)
           "FROM `order` o " +
           "LEFT JOIN customer c ON o.customer_code = c.customer_code " +
           "WHERE (:orderNo IS NULL OR o.order_no LIKE CONCAT('%', :orderNo, '%')) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "AND (:collectionDate IS NULL OR DATE_FORMAT(DATE_ADD(STR_TO_DATE(o.order_dt, '%Y%m%d'), INTERVAL c.collection_day DAY), '%Y%m%d') = :collectionDate) " +
           "ORDER BY o.order_no, DATE_FORMAT(DATE_ADD(STR_TO_DATE(o.order_dt, '%Y%m%d'), INTERVAL c.collection_day DAY), '%Y%m%d'), o.customer_code", 
           nativeQuery = true)
    List<Object[]> findLogisticsPaymentStatus(
        @Param("orderNo") String orderNo,
        @Param("customerCode") Integer customerCode,
        @Param("collectionDate") String collectionDate
    );
    
    /**
     * 배송상태별 주문 조회
     */
    List<Order> findByDeliveryStatus(String deliveryStatus);
    
    /**
     * 거래처별 주문 조회
     */
    List<Order> findByCustomerCode(Integer customerCode);
    
    /**
     * 배송 주문 목록 조회 (배송처리페이지용)
     * @param deliveryRequestDt 납기일자
     * @param customerCode 거래처코드
     * @param orderNo 주문번호 (부분일치)
     * @param deliveryStatus 배송상태
     * @return 조회된 주문 목록
     */
    @Query(value = "SELECT o.* FROM `order` o " +
           "WHERE " +
           "(:deliveryRequestDt IS NULL OR o.delivery_request_dt = :deliveryRequestDt) AND " +
           "(:customerCode IS NULL OR o.customer_code = :customerCode) AND " +
           "(:orderNo IS NULL OR o.order_no LIKE CONCAT('%', :orderNo, '%')) AND " +
           "(:deliveryStatus IS NULL OR o.delivery_status = :deliveryStatus) " +
           "ORDER BY o.delivery_request_dt DESC, o.order_no DESC", nativeQuery = true)
    List<Order> findByDeliverySearchConditions(
        @Param("deliveryRequestDt") String deliveryRequestDt,
        @Param("customerCode") Integer customerCode,
        @Param("orderNo") String orderNo,
        @Param("deliveryStatus") String deliveryStatus
    );
    
    /**
     * 품목별 PickingList 조회
     * 배송요청 상태의 주문에서 품목별 출고량 집계
     */
    @Query(value = "SELECT " +
           "dc.dist_center_name, " +
           "oi.item_code, " +
           "oi.item_name, " +
           "oi.specification, " +
           "oi.unit, " +
           "SUM(oi.order_qty) as total_qty " +
           "FROM `order` o " +
           "JOIN order_item oi ON o.order_no = oi.order_no " +
           "JOIN dist_center dc ON o.dist_center_code = dc.dist_center_code " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "WHERE o.delivery_status = '배송요청' " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR oi.item_code = :itemCode) " +
           "AND (:categoryCode IS NULL OR ( " +
           "    SELECT i.category_code FROM item i WHERE i.item_code = oi.item_code " +
           ") = :categoryCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "AND (:distCenterCode IS NULL OR o.dist_center_code = :distCenterCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "GROUP BY dc.dist_center_name, oi.item_code, oi.item_name, oi.specification, oi.unit " +
           "ORDER BY dc.dist_center_name, oi.item_code", nativeQuery = true)
    List<Object[]> findPickingListByConditions(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("customerCode") Integer customerCode,
        @Param("distCenterCode") Integer distCenterCode,
        @Param("brandCode") Integer brandCode
    );
    
    /**
     * 거래처별원장용 주문 집계 조회 (주문: 배송요청만)
     */
    @Query(value = "SELECT " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "'주문' as order_type, " +
           "SUM(o.total_qty) as total_qty, " +
           "SUM(o.tax_free_amt) as tax_free_amt, " +
           "SUM(o.taxable_amt) as taxable_amt, " +
           "SUM(o.supply_amt) as supply_amt, " +
           "SUM(o.vat_amt) as vat_amt, " +
           "SUM(o.total_amt) as total_amt, " +
           "0 as delivery_qty, " +
           "0 as delivery_supply_amt, " +
           "0 as delivery_vat_amt, " +
           "0 as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status = '배송요청' " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR EXISTS ( " +
           "    SELECT 1 FROM order_item oi WHERE oi.order_no = o.order_no AND oi.item_code = :itemCode " +
           ")) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.customer_code, o.customer_name, b.brand_name, c.tel_num " +
           "ORDER BY o.customer_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerOrderSummary(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 배송 집계 조회 (배송: 배송중 + 배송완료)
     */
    @Query(value = "SELECT " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "'배송' as order_type, " +
           "SUM(o.total_qty) as total_qty, " +
           "SUM(o.tax_free_amt) as tax_free_amt, " +
           "SUM(o.taxable_amt) as taxable_amt, " +
           "SUM(o.supply_amt) as supply_amt, " +
           "SUM(o.vat_amt) as vat_amt, " +
           "SUM(o.total_amt) as total_amt, " +
           "SUM(o.total_qty) as delivery_qty, " +
           "SUM(o.supply_amt) as delivery_supply_amt, " +
           "SUM(o.vat_amt) as delivery_vat_amt, " +
           "SUM(o.total_amt) as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status IN ('배송중', '배송완료') " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR EXISTS ( " +
           "    SELECT 1 FROM order_item oi WHERE oi.order_no = o.order_no AND oi.item_code = :itemCode " +
           ")) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.customer_code, o.customer_name, b.brand_name, c.tel_num " +
           "ORDER BY o.customer_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerDeliverySummary(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 주문 세부 조회 (주문: 배송요청만)
     */
    @Query(value = "SELECT " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "oi.item_code, " +
           "oi.item_name, " +
           "oi.specification, " +
           "oi.unit, " +
           "'주문' as order_type, " +
           "SUM(oi.order_qty) as total_qty, " +
           "SUM(oi.supply_amt) as supply_amt, " +
           "SUM(oi.vat_amt) as vat_amt, " +
           "SUM(oi.total_amt) as total_amt, " +
           "0 as delivery_qty, " +
           "0 as delivery_supply_amt, " +
           "0 as delivery_vat_amt, " +
           "0 as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN order_item oi ON o.order_no = oi.order_no " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status = '배송요청' " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR oi.item_code = :itemCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.customer_code, o.customer_name, b.brand_name, c.tel_num, oi.item_code, oi.item_name, oi.specification, oi.unit " +
           "ORDER BY o.customer_code, oi.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerOrderDetail(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 배송 세부 조회 (배송: 배송중 + 배송완료)
     */
    @Query(value = "SELECT " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "oi.item_code, " +
           "oi.item_name, " +
           "oi.specification, " +
           "oi.unit, " +
           "'배송' as order_type, " +
           "SUM(oi.order_qty) as total_qty, " +
           "SUM(oi.supply_amt) as supply_amt, " +
           "SUM(oi.vat_amt) as vat_amt, " +
           "SUM(oi.total_amt) as total_amt, " +
           "SUM(oi.order_qty) as delivery_qty, " +
           "SUM(oi.supply_amt) as delivery_supply_amt, " +
           "SUM(oi.vat_amt) as delivery_vat_amt, " +
           "SUM(oi.total_amt) as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN order_item oi ON o.order_no = oi.order_no " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status IN ('배송중', '배송완료') " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR oi.item_code = :itemCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.customer_code, o.customer_name, b.brand_name, c.tel_num, oi.item_code, oi.item_name, oi.specification, oi.unit " +
           "ORDER BY o.customer_code, oi.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerDeliveryDetail(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 주문 일자별 조회 (주문: 배송요청만)
     */
    @Query(value = "SELECT " +
           "o.delivery_request_dt as order_date, " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "oi.item_code, " +
           "oi.item_name, " +
           "oi.specification, " +
           "oi.unit, " +
           "'주문' as order_type, " +
           "SUM(oi.order_qty) as total_qty, " +
           "SUM(oi.supply_amt) as supply_amt, " +
           "SUM(oi.vat_amt) as vat_amt, " +
           "SUM(oi.total_amt) as total_amt, " +
           "0 as delivery_qty, " +
           "0 as delivery_supply_amt, " +
           "0 as delivery_vat_amt, " +
           "0 as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN order_item oi ON o.order_no = oi.order_no " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status = '배송요청' " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR oi.item_code = :itemCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.delivery_request_dt, o.customer_code, o.customer_name, b.brand_name, c.tel_num, oi.item_code, oi.item_name, oi.specification, oi.unit " +
           "ORDER BY o.delivery_request_dt, o.customer_code, oi.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerOrderDaily(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 거래처별원장용 배송 일자별 조회 (배송: 배송중 + 배송완료)
     */
    @Query(value = "SELECT " +
           "o.delivery_request_dt as order_date, " +
           "o.customer_code, " +
           "o.customer_name, " +
           "b.brand_name, " +
           "c.tel_num, " +
           "oi.item_code, " +
           "oi.item_name, " +
           "oi.specification, " +
           "oi.unit, " +
           "'배송' as order_type, " +
           "SUM(oi.order_qty) as total_qty, " +
           "SUM(oi.supply_amt) as supply_amt, " +
           "SUM(oi.vat_amt) as vat_amt, " +
           "SUM(oi.total_amt) as total_amt, " +
           "SUM(oi.order_qty) as delivery_qty, " +
           "SUM(oi.supply_amt) as delivery_supply_amt, " +
           "SUM(oi.vat_amt) as delivery_vat_amt, " +
           "SUM(oi.total_amt) as delivery_total_amt " +
           "FROM `order` o " +
           "JOIN order_item oi ON o.order_no = oi.order_no " +
           "JOIN customer c ON o.customer_code = c.customer_code " +
           "JOIN brand_info b ON c.brand_code = b.brand_code " +
           "WHERE o.delivery_status IN ('배송중', '배송완료') " +
           "AND (:deliveryRequestDtStart IS NULL OR o.delivery_request_dt >= :deliveryRequestDtStart) " +
           "AND (:deliveryRequestDtEnd IS NULL OR o.delivery_request_dt <= :deliveryRequestDtEnd) " +
           "AND (:itemCode IS NULL OR oi.item_code = :itemCode) " +
           "AND (:brandCode IS NULL OR c.brand_code = :brandCode) " +
           "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
           "GROUP BY o.delivery_request_dt, o.customer_code, o.customer_name, b.brand_name, c.tel_num, oi.item_code, oi.item_name, oi.specification, oi.unit " +
           "ORDER BY o.delivery_request_dt, o.customer_code, oi.item_code", nativeQuery = true)
    List<Object[]> findCustomerLedgerDeliveryDaily(
        @Param("deliveryRequestDtStart") String deliveryRequestDtStart,
        @Param("deliveryRequestDtEnd") String deliveryRequestDtEnd,
        @Param("itemCode") Integer itemCode,
        @Param("brandCode") Integer brandCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * [앱전용] 최근 주문 정보 조회 (order_dt 기준 최근 1건)
     */
    @Query(value = "SELECT o.order_no, o.total_amt " +
           "FROM `order` o " +
           "WHERE o.customer_code = :customerCode " +
           "ORDER BY o.order_dt DESC " +
           "LIMIT 1", nativeQuery = true)
    Object[] findRecentOrderByCustomerCode(@Param("customerCode") Integer customerCode);
    
    /**
     * 거래처별 납기요청일 범위의 주문내역 조회 (페이징)
     */
    Page<Order> findByCustomerCodeAndDeliveryRequestDtBetween(
            Integer customerCode, 
            String deliveryRequestStartDt, 
            String deliveryRequestEndDt, 
            Pageable pageable);

    /**
     * 거래처별 납기요청일 범위의 주문건수 조회
     */
    Long countByCustomerCodeAndDeliveryRequestDtBetween(
            Integer customerCode, 
            String deliveryRequestStartDt, 
            String deliveryRequestEndDt);

    /**
     * 거래처별 납기요청일 범위의 총 주문금액 조회
     */
    @Query("SELECT SUM(o.totalAmt) FROM Order o WHERE o.customerCode = :customerCode " +
           "AND o.deliveryRequestDt BETWEEN :startDt AND :endDt")
    Integer sumTotalAmtByCustomerCodeAndDeliveryRequestDtBetween(
            @Param("customerCode") Integer customerCode,
            @Param("startDt") String deliveryRequestStartDt,
            @Param("endDt") String deliveryRequestEndDt);
    
    /**
     * 주문번호와 거래처코드로 주문 조회 (권한 체크)
     */
    Order findByOrderNoAndCustomerCode(String orderNo, Integer customerCode);
    
    /**
     * 반품가능한 주문번호 조회 (배송완료 + 납기요청일 1달 이내 + 주문번호 최신순)
     */
    @Query("SELECT o.orderNo FROM Order o " +
           "WHERE o.customerCode = :customerCode " +
           "AND o.deliveryStatus = '배송완료' " +
           "AND o.deliveryRequestDt BETWEEN :oneMonthAgo AND :today " +
           "ORDER BY o.orderNo DESC")
    List<String> findReturnableOrderNumbers(
            @Param("customerCode") Integer customerCode,
            @Param("oneMonthAgo") String oneMonthAgo,
            @Param("today") String today);
}