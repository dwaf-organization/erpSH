package com.inc.sh.repository;

import com.inc.sh.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    
    /**
     * 본사코드 존재 여부 확인
     */
    @Query("SELECT COUNT(h) FROM Headquarter h WHERE h.hqCode = :hqCode")
    Long countByHqCode(@Param("hqCode") Integer hqCode);
    
    /**
     * 물류센터코드 존재 여부 확인
     */
    @Query("SELECT COUNT(d) FROM DistCenter d WHERE d.distCenterCode = :distCenterCode")
    Long countByDistCenterCode(@Param("distCenterCode") Integer distCenterCode);
    
    /**
     * 창고에 재고가 있는 품목 개수 확인
     */
    @Query(value = "SELECT COUNT(*) FROM inventory i WHERE i.warehouse_code = :warehouseCode AND i.current_stock > 0", 
           nativeQuery = true)
    Long countInventoryByWarehouseCode(@Param("warehouseCode") Integer warehouseCode);
    
    /**
     * 창고 검색 조건으로 조회 (물류센터명 포함)
     * @param warehouseCode 창고코드 (완전일치, null 가능)
     * @param distCenterCode 물류센터코드 (완전일치, null 가능)
     * @return 조회된 창고 목록 (창고코드, 창고명, 물류센터명)
     */
    @Query("SELECT w.warehouseCode, w.warehouseName, dc.distCenterName " +
           "FROM Warehouse w LEFT JOIN DistCenter dc ON w.distCenterCode = dc.distCenterCode " +
           "WHERE (:warehouseCode IS NULL OR w.warehouseCode = :warehouseCode) AND " +
           "(:distCenterCode IS NULL OR w.distCenterCode = :distCenterCode) " +
           "ORDER BY w.warehouseCode ASC")
    List<Object[]> findWarehousesWithDistCenterName(
        @Param("warehouseCode") Integer warehouseCode,
        @Param("distCenterCode") Integer distCenterCode
    );
    
    /**
     * 창고 검색 조건으로 조회 (본사별, 물류센터명 포함)
     * @param warehouseCode 창고코드 (완전일치, null 가능)
     * @param distCenterCode 물류센터코드 (완전일치, null 가능)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 창고 목록 (창고코드, 창고명, 물류센터명)
     */
    @Query("SELECT w.warehouseCode, w.warehouseName, dc.distCenterName " +
           "FROM Warehouse w LEFT JOIN DistCenter dc ON w.distCenterCode = dc.distCenterCode " +
           "WHERE (:warehouseCode IS NULL OR w.warehouseCode = :warehouseCode) AND " +
           "(:distCenterCode IS NULL OR w.distCenterCode = :distCenterCode) AND " +
           "w.hqCode = :hqCode " +
           "ORDER BY w.warehouseCode ASC")
    List<Object[]> findWarehousesWithDistCenterNameAndHqCode(
        @Param("warehouseCode") Integer warehouseCode,
        @Param("distCenterCode") Integer distCenterCode,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 창고 검색 조건으로 조회 (본사별, 물류센터명 포함)
     * @param warehouseCode 창고코드 (완전일치, null 가능)
     * @param distCenterCode 물류센터코드 (완전일치, null 가능)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 창고 목록 (창고코드, 창고명, 물류센터명)
     */
    @Query("SELECT w.warehouseCode, w.distCenterCode, w.hqCode, w.warehouseName, \n"
    		+ "       w.zipCode, w.addr, w.telNum, w.managerName, w.managerContact, \n"
    		+ "       w.useYn, w.description, w.createdAt, w.updatedAt, dc.distCenterName " +
           "FROM Warehouse w LEFT JOIN DistCenter dc ON w.distCenterCode = dc.distCenterCode " +
           "WHERE (:warehouseCode IS NULL OR w.warehouseCode = :warehouseCode) AND " +
           "(:distCenterCode IS NULL OR w.distCenterCode = :distCenterCode) AND " +
           "w.hqCode = :hqCode " +
           "ORDER BY w.warehouseCode ASC")
    List<Object[]> findWarehousesWithDistCenterNameAndHqCodeAll(
        @Param("warehouseCode") Integer warehouseCode,
        @Param("distCenterCode") Integer distCenterCode,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 창고코드로 조회
     */
    Warehouse findByWarehouseCode(Integer warehouseCode);
    
    /**
     * 본사별 창고 조회
     */
    List<Warehouse> findByHqCode(Integer hqCode);
    
    /**
     * 물류센터별 창고 조회
     */
    List<Warehouse> findByDistCenterCode(Integer distCenterCode);
    
    /**
     * 물류센터별 창고 코드 목록 조회 (물류센터 삭제시 사용)
     */
    @Query("SELECT w.warehouseCode FROM Warehouse w WHERE w.distCenterCode = :distCenterCode")
    List<Integer> findWarehouseCodesByDistCenterCode(@Param("distCenterCode") Integer distCenterCode);
    
    /**
     * 물류센터에 연결된 창고 존재 여부 확인
     */
    boolean existsByDistCenterCode(Integer distCenterCode);
    
    /**
     * 사용중인 창고 조회
     */
    @Query("SELECT w FROM Warehouse w WHERE w.useYn = 1 ORDER BY w.warehouseCode ASC")
    List<Warehouse> findActiveWarehouses();
}