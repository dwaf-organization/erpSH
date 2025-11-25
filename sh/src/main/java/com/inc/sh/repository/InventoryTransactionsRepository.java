package com.inc.sh.repository;

import com.inc.sh.entity.InventoryTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionsRepository extends JpaRepository<InventoryTransactions, Integer> {
    
    /**
     * 창고품목코드별 재고수불부 조회
     */
    List<InventoryTransactions> findByWarehouseItemCode(@Param("warehouseItemCode") Integer warehouseItemCode);
    
    /**
     * 창고별 재고수불부 조회
     */
    List<InventoryTransactions> findByWarehouseCode(@Param("warehouseCode") Integer warehouseCode);
    
    /**
     * 품목별 재고수불부 조회
     */
    List<InventoryTransactions> findByItemCode(@Param("itemCode") Integer itemCode);
    
    /**
     * 거래일자별 재고수불부 조회
     */
    List<InventoryTransactions> findByTransactionDate(@Param("transactionDate") String transactionDate);
    
    /**
     * 조정유형별 재고수불부 조회
     */
    List<InventoryTransactions> findByTransactionType(@Param("transactionType") String transactionType);

    /**
     * 재고수불부 조회 (분류명 계층구조 포함)
     */
    @Query(value = "SELECT " +
           "it.warehouse_code, " +
           "CASE " +
           "  WHEN ic1.parents_category_code = 0 THEN ic1.category_name " +
           "  ELSE CONCAT(ic2.category_name, '-', ic1.category_name) " +
           "END as category_display_name, " +
           "it.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "it.transaction_date, " +
           "it.transaction_type, " +
           "it.quantity, " +
           "it.unit_price, " +
           "it.amount " +
           "FROM inventory_transactions it " +
           "JOIN item i ON it.item_code = i.item_code " +
           "JOIN item_category ic1 ON i.category_code = ic1.category_code " +
           "LEFT JOIN item_category ic2 ON ic1.parents_category_code = ic2.category_code " +
           "WHERE it.transaction_date >= :startDate " +
           "AND it.transaction_date <= :endDate " +
           "AND (:warehouseCode IS NULL OR it.warehouse_code = :warehouseCode) " +
           "AND (:categoryCode IS NULL OR i.category_code = :categoryCode) " +
           "AND (:itemCodeSearch IS NULL OR it.item_code LIKE CONCAT('%', :itemCodeSearch, '%')) " +
           "ORDER BY it.transaction_date, it.warehouse_code, it.item_code", nativeQuery = true)
    List<Object[]> findInventoryTransactionSummary(
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("warehouseCode") Integer warehouseCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("itemCodeSearch") String itemCodeSearch
    );
    
    /**
     * 창고품목코드 목록으로 재고수불부 삭제
     */
    void deleteByWarehouseItemCodeIn(@Param("warehouseItemCodes") List<Integer> warehouseItemCodes);
    
    /**
     * 창고품목코드로 재고수불부 삭제
     */
    void deleteByWarehouseItemCode(@Param("warehouseItemCode") Integer warehouseItemCode);
}