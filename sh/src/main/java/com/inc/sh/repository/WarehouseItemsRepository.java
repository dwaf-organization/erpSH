package com.inc.sh.repository;

import com.inc.sh.entity.WarehouseItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseItemsRepository extends JpaRepository<WarehouseItems, Integer> {
    
    /**
     * 창고코드와 품목코드로 창고품목 조회
     */
    Optional<WarehouseItems> findByWarehouseCodeAndItemCode(@Param("warehouseCode") Integer warehouseCode, @Param("itemCode") Integer itemCode);
    
    /**
     * 창고별 품목 목록 조회
     */
    List<WarehouseItems> findByWarehouseCode(@Param("warehouseCode") Integer warehouseCode);
    
    /**
     * 품목별 창고 목록 조회
     */
    List<WarehouseItems> findByItemCode(@Param("itemCode") Integer itemCode);
    
    /**
     * 창고에 품목이 존재하는지 확인
     */
    boolean existsByWarehouseCode(@Param("warehouseCode") Integer warehouseCode);
    
    /**
     * 창고별 품목 개수 조회
     */
    @Query("SELECT COUNT(wi) FROM WarehouseItems wi WHERE wi.warehouseCode = :warehouseCode")
    Long countByWarehouseCode(@Param("warehouseCode") Integer warehouseCode);

    /**
     * 출고창고 품목 조회 (이송용)
     */
    @Query(value = "SELECT " +
           "wi.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "wi.current_quantity, " +
           "i.base_price " +
           "FROM warehouse_items wi " +
           "JOIN item i ON wi.item_code = i.item_code " +
           "WHERE wi.warehouse_code = :warehouseCode " +
           "ORDER BY wi.item_code", nativeQuery = true)
    List<Object[]> findWarehouseItemsForTransfer(@Param("warehouseCode") Integer warehouseCode);

    /**
     * 출고창고 품목 조회 (이송용, 본사별)
     */
    @Query(value = "SELECT " +
           "wi.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "wi.current_quantity, " +
           "i.base_price " +
           "FROM warehouse_items wi " +
           "JOIN item i ON wi.item_code = i.item_code " +
           "JOIN warehouse w ON wi.warehouse_code = w.warehouse_code " +
           "WHERE wi.warehouse_code = :warehouseCode " +
           "AND w.hq_code = :hqCode " +
           "ORDER BY wi.item_code", nativeQuery = true)
    List<Object[]> findWarehouseItemsForTransferWithHqCode(
        @Param("warehouseCode") Integer warehouseCode,
        @Param("hqCode") Integer hqCode
    );
}