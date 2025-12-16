package com.inc.sh.repository;

import com.inc.sh.entity.DistrictAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisTestRepository extends JpaRepository<DistrictAnalysis, Integer> {
    
    /**
     * 구코드로 상권분석 조회
     */
    DistrictAnalysis findByDistrictCode(@Param("districtCode") Integer districtCode);
    
    /**
     * 구코드 존재 여부 확인
     */
    boolean existsByDistrictCode(@Param("districtCode") Integer districtCode);
    
    /**
     * 전체 상권분석 목록 조회 (구코드 순)
     */
    @Query("SELECT da FROM DistrictAnalysis da ORDER BY da.districtCode ASC")
    List<DistrictAnalysis> findAllOrderByDistrictCode();
    
    /**
     * 성장 코드별 상권분석 조회 (1=상승, 0=하락)
     */
    @Query("SELECT da FROM DistrictAnalysis da WHERE da.growthCode = :growthCode ORDER BY da.growthRate DESC")
    List<DistrictAnalysis> findByGrowthCodeOrderByGrowthRateDesc(@Param("growthCode") Integer growthCode);
    
    /**
     * 매출 범위별 상권분석 조회
     */
    @Query("SELECT da FROM DistrictAnalysis da " +
           "WHERE da.currentSales BETWEEN :minSales AND :maxSales " +
           "ORDER BY da.currentSales DESC")
    List<DistrictAnalysis> findByCurrentSalesBetweenOrderByCurrentSalesDesc(
        @Param("minSales") Long minSales, 
        @Param("maxSales") Long maxSales
    );
}