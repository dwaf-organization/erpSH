package com.inc.sh.repository;

import com.inc.sh.entity.WarehouseTransfers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseTransfersRepository extends JpaRepository<WarehouseTransfers, String> {
    
    /**
     * 이송일자별 이송내역 조회
     */
    List<WarehouseTransfers> findByTransferDate(@Param("transferDate") String transferDate);
    
    /**
     * 출고창고별 이송내역 조회
     */
    List<WarehouseTransfers> findByFromWarehouseCode(@Param("fromWarehouseCode") Integer fromWarehouseCode);
    
    /**
     * 입고창고별 이송내역 조회
     */
    List<WarehouseTransfers> findByToWarehouseCode(@Param("toWarehouseCode") Integer toWarehouseCode);
    
    /**
     * 특정 날짜의 마지막 이송번호 조회 (시퀀스 생성용)
     */
    @Query(value = "SELECT transfer_code FROM warehouse_transfers " +
           "WHERE transfer_code LIKE CONCAT('TR', :dateStr, '-%') " +
           "ORDER BY transfer_code DESC LIMIT 1", nativeQuery = true)
    String findLastTransferCodeByDate(@Param("dateStr") String dateStr);

    /**
     * 창고이송현황 조회 (년월 범위, 창고 조건)
     */
    @Query(value = "SELECT " +
           "wt.transfer_code, " +
           "wt.transfer_date, " +
           "wt.from_warehouse_code, " +
           "w1.warehouse_name as from_warehouse_name, " +
           "wt.to_warehouse_code, " +
           "w2.warehouse_name as to_warehouse_name, " +
           "wt.note " +
           "FROM warehouse_transfers wt " +
           "JOIN warehouse w1 ON wt.from_warehouse_code = w1.warehouse_code " +
           "JOIN warehouse w2 ON wt.to_warehouse_code = w2.warehouse_code " +
           "WHERE SUBSTR(wt.transfer_date, 1, 6) >= :startYm " +
           "AND SUBSTR(wt.transfer_date, 1, 6) <= :endYm " +
           "AND (:fromWarehouseCode IS NULL OR wt.from_warehouse_code = :fromWarehouseCode) " +
           "AND (:toWarehouseCode IS NULL OR wt.to_warehouse_code = :toWarehouseCode) " +
           "ORDER BY wt.transfer_date, wt.transfer_code", nativeQuery = true)
    List<Object[]> findWarehouseTransferList(
        @Param("startYm") String startYm,
        @Param("endYm") String endYm,
        @Param("fromWarehouseCode") Integer fromWarehouseCode,
        @Param("toWarehouseCode") Integer toWarehouseCode
    );
}