package com.inc.sh.repository;

import com.inc.sh.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
    
    /**
     * 차량코드로 조회
     */
    Vehicle findByVehicleCode(Integer vehicleCode);
    
    /**
     * 차량코드 존재 여부 확인
     */
    boolean existsByVehicleCode(Integer vehicleCode);
    
    /**
     * 검색 조건으로 차량 조회
     * @param vehicleCode 차량코드 (완전일치, null 가능)
     * @param category 구분 (냉장,냉동,상온, null 가능)
     * @return 조회된 차량 목록
     */
    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:vehicleCode IS NULL OR v.vehicleCode = :vehicleCode) AND " +
           "(:category IS NULL OR v.category = :category) " +
           "ORDER BY v.vehicleCode ASC")
    List<Vehicle> findBySearchConditions(
        @Param("vehicleCode") Integer vehicleCode,
        @Param("category") String category
    );
    
    /**
     * 본사별 차량 조회
     */
    List<Vehicle> findByHqCode(Integer hqCode);
    
    /**
     * 구분별 차량 조회
     */
    List<Vehicle> findByCategory(String category);
    
    /**
     * 본사별 차량 목록 조회 (셀렉트박스용)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.hqCode = :hqCode ORDER BY v.vehicleCode")
    List<Vehicle> findByHqCodeOrderByVehicleCode(@Param("hqCode") Integer hqCode);
    
    /**
     * 모든 차량 조회 (셀렉트박스용)
     */
    @Query("SELECT v FROM Vehicle v ORDER BY v.vehicleCode")
    List<Vehicle> findAllOrderByVehicleCode();
}