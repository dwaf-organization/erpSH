package com.inc.sh.repository;

import com.inc.sh.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxInvoiceOrderRepository extends JpaRepository<Order, String> {

    /**
     * 관리자 - 전자세금계산서 발행: 거래처별 매출 집계 (배송완료 기준)
     */
    @Query(value = 
        "SELECT " +
        "   c.customer_code, " +
        "   c.customer_name, " +
        "   c.owner_name, " +
        "   c.biz_num, " +
        "   c.addr, " +
        "   GROUP_CONCAT(DISTINCT oi.item_name ORDER BY oi.item_name SEPARATOR ', ') as item_names, " +
        "   SUM(oi.tax_free_amt) as tax_free_supply_amt, " +
        "   SUM(oi.taxable_amt) as taxable_supply_amt, " +
        "   SUM(oi.vat_amt) as vat_amt " +
        "FROM `order` o " +
        "INNER JOIN customer c ON o.customer_code = c.customer_code " +
        "INNER JOIN order_item oi ON o.order_no = oi.order_no " +
        "WHERE o.hq_code = :hqCode " +
        "AND o.delivery_status = '배송완료' " +
        "AND o.delivery_dt BETWEEN :startDate AND :endDate " +
        "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
        "AND (:itemCodesSize = 0 OR oi.item_code IN :itemCodes) " +
        "GROUP BY c.customer_code, c.customer_name, c.owner_name, c.biz_num, c.addr " +
        "ORDER BY c.customer_code ASC", 
        nativeQuery = true)
    List<Object[]> findTaxInvoiceDataByConditions(
        @Param("hqCode") Integer hqCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("itemCodes") List<Integer> itemCodes,
        @Param("itemCodesSize") int itemCodesSize
    );

    /**
     * 관리자 - 전자세금계산서 발행: 반품 차감 데이터 조회 (승인된 반품 기준)
     */
    @Query(value = 
        "SELECT " +
        "   o.customer_code, " +
        "   SUM(CASE WHEN oi.tax_target = '면세' THEN (r.qty * oi.order_unit_price) ELSE 0 END) as return_tax_free_amt, " +
        "   SUM(CASE WHEN oi.tax_target = '과세' THEN (r.qty * oi.order_unit_price) ELSE 0 END) as return_taxable_amt, " +
        "   SUM(CASE WHEN oi.tax_target = '과세' THEN (r.qty * oi.order_unit_price * 0.1) ELSE 0 END) as return_vat_amt " +
        "FROM `return` r " +
        "INNER JOIN `order` o ON r.order_no = o.order_no " +
        "INNER JOIN order_item oi ON r.order_item_code = oi.order_item_code " +
        "WHERE o.hq_code = :hqCode " +
        "AND r.progress_status = '승인' " +
        "AND r.return_approve_dt BETWEEN :startDate AND :endDate " +
        "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
        "AND (:itemCodesSize = 0 OR oi.item_code IN :itemCodes) " +
        "GROUP BY o.customer_code " +
        "ORDER BY o.customer_code ASC", 
        nativeQuery = true)
    List<Object[]> findReturnDataByConditions(
        @Param("hqCode") Integer hqCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("itemCodes") List<Integer> itemCodes,
        @Param("itemCodesSize") int itemCodesSize
    );

    /**
     * 관리자 - 전자세금계산서 발행: 품목명 갯수 계산용 (첫 품목 + 나머지 갯수)
     */
    @Query(value = 
        "SELECT " +
        "   o.customer_code, " +
        "   COUNT(DISTINCT oi.item_code) as item_count, " +
        "   MIN(oi.item_name) as first_item_name " +
        "FROM `order` o " +
        "INNER JOIN order_item oi ON o.order_no = oi.order_no " +
        "WHERE o.hq_code = :hqCode " +
        "AND o.delivery_status = '배송완료' " +
        "AND o.delivery_dt BETWEEN :startDate AND :endDate " +
        "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
        "AND (:itemCodesSize = 0 OR oi.item_code IN :itemCodes) " +
        "GROUP BY o.customer_code " +
        "ORDER BY o.customer_code ASC", 
        nativeQuery = true)
    List<Object[]> findItemNamesByConditions(
        @Param("hqCode") Integer hqCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("itemCodes") List<Integer> itemCodes,
        @Param("itemCodesSize") int itemCodesSize
    );
}