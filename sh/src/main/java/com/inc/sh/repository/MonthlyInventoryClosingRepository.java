package com.inc.sh.repository;

import com.inc.sh.entity.MonthlyInventoryClosing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyInventoryClosingRepository extends JpaRepository<MonthlyInventoryClosing, Integer> {
    
    /**
     * 년월별 월별재고마감 조회
     */
    List<MonthlyInventoryClosing> findByClosingYm(@Param("closingYm") String closingYm);
    
    /**
     * 창고별 년월별 월별재고마감 조회
     */
    List<MonthlyInventoryClosing> findByWarehouseCodeAndClosingYm(@Param("warehouseCode") Integer warehouseCode, @Param("closingYm") String closingYm);
    
    /**
     * 창고품목코드와 년월로 월별재고마감 조회
     */
    Optional<MonthlyInventoryClosing> findByWarehouseItemCodeAndClosingYm(@Param("warehouseItemCode") Integer warehouseItemCode, @Param("closingYm") String closingYm);
    
    /**
     * 창고코드, 품목코드, 년월로 월별재고마감 조회 (OrderService용)
     */
    Optional<MonthlyInventoryClosing> findByWarehouseCodeAndItemCodeAndClosingYm(
        @Param("warehouseCode") Integer warehouseCode, 
        @Param("itemCode") Integer itemCode, 
        @Param("closingYm") String closingYm);
    
    /**
     * 재고등록용 재고 조회 (품목코드 OR 품명 검색 지원)
     */
    @Query(value = "SELECT " +
           "mic.warehouse_item_code, " +
           "mic.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "mic.actual_unit_price, " +
           "mic.actual_quantity, " +
           "mic.actual_amount, " +
           "wi.current_quantity, " +
           "wi.safe_quantity, " +
           "w.warehouse_name, " +
           "mic.closing_ym " +
           "FROM monthly_inventory_closing mic " +
           "JOIN item i ON mic.item_code = i.item_code " +
           "JOIN warehouse w ON mic.warehouse_code = w.warehouse_code " +
           "JOIN warehouse_items wi ON mic.warehouse_item_code = wi.warehouse_item_code " +
           "WHERE mic.closing_ym = :closingYm " +
           "AND (:warehouseCode IS NULL OR mic.warehouse_code = :warehouseCode) " +
           "AND (:itemSearch IS NULL OR " +
           "    mic.item_code = :itemSearch OR " +
           "    i.item_name LIKE CONCAT('%', :itemSearch, '%') " +
           ") " +
           "ORDER BY mic.warehouse_code, mic.item_code", nativeQuery = true)
    List<Object[]> findInventoryByConditions(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("itemSearch") String itemSearch
    );
    
    /**
     * 창고품목코드 목록으로 월별재고마감 삭제
     */
    void deleteByWarehouseItemCodeIn(@Param("warehouseItemCodes") List<Integer> warehouseItemCodes);
    
    /**
     * 창고품목코드로 월별재고마감 삭제
     */
    void deleteByWarehouseItemCode(@Param("warehouseItemCode") Integer warehouseItemCode);
    
    /**
     * 재고실사 조회 (월별재고마감 + 품목정보 + 최근거래유형)
     */
    @Query(value = "SELECT " +
           "mic.closing_code, " +
           "mic.item_code, " +
           "i.item_name, " +
           "COALESCE(ic.category_name, '미분류') as category_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "COALESCE(( " +
           "  SELECT it.transaction_type " +
           "  FROM inventory_transactions it " +
           "  WHERE it.item_code = mic.item_code " +
           "  AND it.warehouse_code = mic.warehouse_code " +
           "  ORDER BY it.created_at DESC " +
           "  LIMIT 1 " +
           "), '거래없음') as transaction_type, " +
           "mic.opening_quantity, " +
           "mic.opening_amount, " +
           "mic.in_quantity, " +
           "mic.in_amount, " +
           "mic.out_quantity, " +
           "mic.out_amount, " +
           "mic.cal_quantity, " +
           "mic.cal_amount, " +
           "mic.actual_quantity, " +
           "mic.actual_unit_price, " +
           "mic.actual_amount, " +
           "mic.diff_quantity, " +
           "mic.diff_amount " +
           "FROM monthly_inventory_closing mic " +
           "JOIN item i ON mic.item_code = i.item_code " +
           "LEFT JOIN item_category ic ON i.category_code = ic.category_code " +
           "WHERE mic.closing_ym = :closingYm " +
           "AND (:warehouseCode IS NULL OR mic.warehouse_code = :warehouseCode) " +
           "AND (:itemSearch IS NULL OR " +
           "    mic.item_code = :itemSearch OR " +
           "    i.item_name LIKE CONCAT('%', :itemSearch, '%') " +
           ") " +
           "ORDER BY ic.category_code, mic.item_code", nativeQuery = true)
    List<Object[]> findInventoryInspectionByConditions(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("itemSearch") String itemSearch
    );

    /**
     * 월재고마감 현황 조회 (창고별 그룹핑)
     */
    @Query(value = "SELECT " +
           "mic.warehouse_code, " +
           "w.warehouse_name, " +
           "mic.closing_ym, " +
           "MIN(mic.is_closed) as is_closed, " +
           "DATE(MAX(mic.closed_at)) as closed_date, " +
           "MAX(mic.closed_user) as closed_user " +
           "FROM monthly_inventory_closing mic " +
           "JOIN warehouse w ON mic.warehouse_code = w.warehouse_code " +
           "WHERE mic.closing_ym = :closingYm " +
           "AND (:warehouseCode IS NULL OR mic.warehouse_code = :warehouseCode) " +
           "GROUP BY mic.warehouse_code, w.warehouse_name, mic.closing_ym " +
           "ORDER BY mic.warehouse_code", nativeQuery = true)
    List<Object[]> findMonthlyClosingByConditions(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode
    );

    /**
     * 창고별 년월별 마감 상태 조회
     */
    @Query(value = "SELECT MIN(is_closed) FROM monthly_inventory_closing " +
           "WHERE closing_ym = :closingYm AND warehouse_code = :warehouseCode", nativeQuery = true)
    Integer findClosingStatusByWarehouseAndYm(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode
    );

    /**
     * 창고별 년월별 모든 마감 데이터 업데이트
     */
    @Modifying
    @Query(value = "UPDATE monthly_inventory_closing " +
           "SET is_closed = :isClosed, " +
           "closed_at = :closedAt, " +
           "closed_user = :closedUser, " +
           "updated_at = CURRENT_TIMESTAMP " +
           "WHERE closing_ym = :closingYm AND warehouse_code = :warehouseCode", nativeQuery = true)
    int updateClosingStatusByWarehouseAndYm(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("isClosed") Integer isClosed,
        @Param("closedAt") LocalDateTime closedAt,
        @Param("closedUser") String closedUser
    );

    /**
     * 월재고현황 조회 (분류명 계층구조 포함)
     */
    @Query(value = "SELECT " +
           "mic.warehouse_code, " +
           "CASE " +
           "  WHEN ic1.parents_category_code = 0 THEN ic1.category_name " +
           "  ELSE CONCAT(ic2.category_name, '-', ic1.category_name) " +
           "END as category_display_name, " +
           "mic.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "mic.opening_quantity, " +
           "mic.opening_amount, " +
           "mic.in_quantity, " +
           "mic.in_amount, " +
           "mic.out_quantity, " +
           "mic.out_amount, " +
           "mic.cal_quantity, " +
           "mic.actual_quantity, " +
           "mic.actual_unit_price, " +
           "mic.actual_amount, " +
           "mic.diff_quantity, " +
           "mic.diff_amount " +
           "FROM monthly_inventory_closing mic " +
           "JOIN item i ON mic.item_code = i.item_code " +
           "JOIN item_category ic1 ON i.category_code = ic1.category_code " +
           "LEFT JOIN item_category ic2 ON ic1.parents_category_code = ic2.category_code " +
           "WHERE mic.closing_ym = :closingYm " +
           "AND (:warehouseCode IS NULL OR mic.warehouse_code = :warehouseCode) " +
           "AND (:categoryCode IS NULL OR i.category_code = :categoryCode) " +
           "AND (:itemSearch IS NULL OR mic.item_code LIKE CONCAT('%', :itemSearch, '%')) " +
           "ORDER BY mic.warehouse_code, ic1.category_code, mic.item_code", nativeQuery = true)
    List<Object[]> findMonthlyInventoryStatusByConditions(
        @Param("closingYm") String closingYm,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("itemSearch") String itemSearch
    );
}