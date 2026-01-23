package com.inc.sh.repository;

import com.inc.sh.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxInvoiceExcelRepository extends JpaRepository<Order, String> {

    /**
     * 관리자 - 전자세금계산서 엑셀: 본사정보 + 거래처별 매출 집계 (배송완료 기준)
     */
    @Query(value = 
        "SELECT " +
        "   h.biz_num as hq_biz_num, " +
        "   h.company_name as supplier_company_name, " +
        "   h.ceo_name as supplier_ceo_name, " +
        "   h.addr as supplier_addr, " +
        "   h.biz_type as supplier_biz_type, " +
        "   h.biz_item as supplier_biz_sector, " +
        "   c.customer_code, " +
        "   c.biz_num as customer_biz_num, " +
        "   c.customer_name, " +
        "   c.owner_name, " +
        "   c.addr, " +
        "   c.biz_type as customer_biz_type, " +
        "   c.biz_sector as customer_biz_sector, " +
        "   c.email as customer_email, " +
        "   GROUP_CONCAT(DISTINCT oi.item_name ORDER BY oi.item_name SEPARATOR ', ') as item_names, " +
        "   SUM(oi.tax_free_amt) as tax_free_supply_amt, " +
        "   SUM(oi.taxable_amt) as taxable_supply_amt, " +
        "   SUM(oi.vat_amt) as vat_amt " +
        "FROM `order` o " +
        "INNER JOIN customer c ON o.customer_code = c.customer_code " +
        "INNER JOIN headquarter h ON o.hq_code = h.hq_code " +
        "INNER JOIN order_item oi ON o.order_no = oi.order_no " +
        "WHERE o.hq_code = :hqCode " +
        "AND o.delivery_status = '배송완료' " +
        "AND o.delivery_dt BETWEEN :startDate AND :endDate " +
        "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
        "AND (:itemCodesSize = 0 OR oi.item_code IN :itemCodes) " +
        "GROUP BY h.biz_num, h.company_name, h.ceo_name, h.addr, h.biz_type, h.biz_item, " +
        "         c.customer_code, c.biz_num, c.biz_type, c.biz_sector, c.email " +
        "ORDER BY c.customer_code ASC", 
        nativeQuery = true)
    List<Object[]> findTaxInvoiceExcelDataByConditions(
        @Param("hqCode") Integer hqCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("itemCodes") List<Integer> itemCodes,
        @Param("itemCodesSize") int itemCodesSize
    );

    /**
     * 관리자 - 전자세금계산서 엑셀: 반품 차감 데이터 조회 (승인된 반품 기준)
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
    List<Object[]> findReturnExcelDataByConditions(
        @Param("hqCode") Integer hqCode,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("customerCode") Integer customerCode,
        @Param("itemCodes") List<Integer> itemCodes,
        @Param("itemCodesSize") int itemCodesSize
    );
}