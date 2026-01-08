package com.inc.sh.repository;

import com.inc.sh.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Integer> {
    
    /**
     * 플랫폼별, 날짜별 집계 데이터 조회
     */
    DailySalesSummary findByPlatformAndSummaryDateAndBrandCode(String platform, String summaryDate, Integer brandCode);
    
    /**
     * 브랜드별 집계 데이터 조회
     */
    List<DailySalesSummary> findByBrandCodeOrderBySummaryDateDesc(Integer brandCode);
    
    /**
     * 플랫폼별 집계 데이터 조회
     */
    List<DailySalesSummary> findByPlatformAndBrandCodeOrderBySummaryDateDesc(String platform, Integer brandCode);
    
    /**
     * 기간별 집계 데이터 조회
     */
    List<DailySalesSummary> findBySummaryDateBetweenAndBrandCodeOrderBySummaryDateDesc(
            String startDate, String endDate, Integer brandCode);
}