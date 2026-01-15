package com.inc.sh.repository;

import com.inc.sh.entity.PublicDataAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 공공데이터 분석 Repository
 */
@Repository
public interface PublicDataAnalysisRepository extends JpaRepository<PublicDataAnalysis, Long> {

    /**
     * 행정동코드로 조회
     */
    Optional<PublicDataAnalysis> findByAdminDongCode(Integer adminDongCode);

    /**
     * 행정동코드 존재 여부 확인
     */
    boolean existsByAdminDongCode(Integer adminDongCode);

    /**
     * 행정동이름으로 조회
     */
    List<PublicDataAnalysis> findByAdminDongNameContaining(String adminDongName);

    /**
     * 최근 업데이트 순으로 조회
     */
    List<PublicDataAnalysis> findAllByOrderByUpdatedAtDesc();

    /**
     * 특정 날짜 이후 업데이트된 데이터 조회
     */
    List<PublicDataAnalysis> findByUpdatedAtAfter(LocalDateTime dateTime);

    /**
     * 매출 기준 상위 N개 조회
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "ORDER BY (p.koreanSales + p.chineseSales + p.japaneseSales + p.westernSales + p.southeastAsianSales) DESC")
    List<PublicDataAnalysis> findTopBySalesOrderByTotalSalesDesc();

    /**
     * 업소수 기준 상위 N개 조회
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "ORDER BY (p.koreanRestaurantCount + p.chineseRestaurantCount + p.japaneseRestaurantCount + " +
           "p.westernRestaurantCount + p.southeastAsianRestaurantCount) DESC")
    List<PublicDataAnalysis> findTopByRestaurantCountOrderByTotalCountDesc();

    /**
     * 인구수 기준 상위 N개 조회
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "ORDER BY (p.floatingPopulation + p.residentialPopulation + p.workingPopulation) DESC")
    List<PublicDataAnalysis> findTopByPopulationOrderByTotalPopulationDesc();

    /**
     * 특정 매출 범위 내 데이터 조회
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "WHERE (p.koreanSales + p.chineseSales + p.japaneseSales + p.westernSales + p.southeastAsianSales) " +
           "BETWEEN :minSales AND :maxSales")
    List<PublicDataAnalysis> findBySalesRange(@Param("minSales") Long minSales, 
                                            @Param("maxSales") Long maxSales);

    /**
     * 특정 인구수 범위 내 데이터 조회
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "WHERE (p.floatingPopulation + p.residentialPopulation + p.workingPopulation) " +
           "BETWEEN :minPopulation AND :maxPopulation")
    List<PublicDataAnalysis> findByPopulationRange(@Param("minPopulation") Integer minPopulation,
                                                  @Param("maxPopulation") Integer maxPopulation);

    /**
     * 통계 쿼리 - 매출 합계
     */
    @Query("SELECT SUM(p.koreanSales + p.chineseSales + p.japaneseSales + p.westernSales + p.southeastAsianSales) " +
           "FROM PublicDataAnalysis p")
    Long getTotalSalesSum();

    /**
     * 통계 쿼리 - 업소수 합계
     */
    @Query("SELECT SUM(p.koreanRestaurantCount + p.chineseRestaurantCount + p.japaneseRestaurantCount + " +
           "p.westernRestaurantCount + p.southeastAsianRestaurantCount) " +
           "FROM PublicDataAnalysis p")
    Long getTotalRestaurantCountSum();

    /**
     * 통계 쿼리 - 인구수 합계
     */
    @Query("SELECT SUM(p.floatingPopulation + p.residentialPopulation + p.workingPopulation) " +
           "FROM PublicDataAnalysis p")
    Long getTotalPopulationSum();

    /**
     * 행정동코드 리스트로 조회
     */
    List<PublicDataAnalysis> findByAdminDongCodeIn(List<Integer> adminDongCodes);

    /**
     * 데이터 완성도 체크 (매출 데이터가 있는 경우)
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "WHERE (p.koreanSales > 0 OR p.chineseSales > 0 OR p.japaneseSales > 0 " +
           "OR p.westernSales > 0 OR p.southeastAsianSales > 0)")
    List<PublicDataAnalysis> findWithSalesData();

    /**
     * 데이터 완성도 체크 (인구 데이터가 있는 경우)
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "WHERE (p.floatingPopulation > 0 OR p.residentialPopulation > 0 OR p.workingPopulation > 0)")
    List<PublicDataAnalysis> findWithPopulationData();

    /**
     * 불완전한 데이터 조회 (매출 또는 인구 데이터 중 하나라도 없는 경우)
     */
    @Query("SELECT p FROM PublicDataAnalysis p " +
           "WHERE (p.koreanSales = 0 AND p.chineseSales = 0 AND p.japaneseSales = 0 " +
           "AND p.westernSales = 0 AND p.southeastAsianSales = 0) " +
           "OR (p.floatingPopulation = 0 AND p.residentialPopulation = 0 AND p.workingPopulation = 0)")
    List<PublicDataAnalysis> findIncompleteData();
}