package com.inc.sh.repository;

import com.inc.sh.entity.WarehouseTransfersItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseTransfersItemsRepository extends JpaRepository<WarehouseTransfersItems, Integer> {
    
    /**
     * 이송번호별 이송품목 조회
     */
    List<WarehouseTransfersItems> findByTransferCode(@Param("transferCode") String transferCode);
    
    /**
     * 품목코드별 이송품목 조회
     */
    List<WarehouseTransfersItems> findByItemCode(@Param("itemCode") Integer itemCode);
    
    /**
     * 이송번호로 이송품목 삭제
     */
    void deleteByTransferCode(@Param("transferCode") String transferCode);

    /**
     * 이송번호별 이송품목 상세 조회 (품목정보 포함)
     */
    @Query(value = "SELECT " +
           "wti.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "wti.unit_price, " +
           "wti.quantity, " +
           "wti.amount " +
           "FROM warehouse_transfers_items wti " +
           "JOIN item i ON wti.item_code = i.item_code " +
           "WHERE wti.transfer_code = :transferCode " +
           "ORDER BY wti.item_code", nativeQuery = true)
    List<Object[]> findWarehouseTransferItemsByTransferCode(@Param("transferCode") String transferCode);
}