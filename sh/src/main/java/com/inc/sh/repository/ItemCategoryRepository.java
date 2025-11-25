package com.inc.sh.repository;

import com.inc.sh.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Integer> {
    
    /**
     * 분류코드로 품목분류 조회
     */
    ItemCategory findByCategoryCode(Integer categoryCode);
    
    /**
     * 분류코드 존재 여부 확인
     */
    boolean existsByCategoryCode(Integer categoryCode);
    
    /**
     * 전체 품목분류 조회 (계층구조 정렬)
     * 상위분류코드, 분류순서 순으로 정렬
     */
    @Query("SELECT ic FROM ItemCategory ic ORDER BY ic.parentsCategoryCode, ic.categoryLevel, ic.categoryCode")
    List<ItemCategory> findAllOrderByHierarchy();
    
    /**
     * 본사별 품목분류 조회 (계층구조 정렬)
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.hqCode = :hqCode ORDER BY ic.parentsCategoryCode, ic.categoryLevel, ic.categoryCode")
    List<ItemCategory> findByHqCodeOrderByHierarchy(@Param("hqCode") Integer hqCode);
    
    /**
     * 특정 분류와 그 하위 분류 조회
     * @param categoryCode 기준 분류코드
     * @return 본인 + 하위 분류 목록
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.categoryCode = :categoryCode OR ic.parentsCategoryCode = :categoryCode ORDER BY ic.categoryLevel, ic.categoryCode")
    List<ItemCategory> findByParentAndChildren(@Param("categoryCode") Integer categoryCode);
    
    /**
     * 상위 분류별 하위 분류 조회
     */
    List<ItemCategory> findByParentsCategoryCodeOrderByCategoryLevel(Integer parentsCategoryCode);
    
    /**
     * 대분류만 조회 (parentsCategoryCode = 0)
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.parentsCategoryCode = 0 ORDER BY ic.categoryLevel, ic.categoryCode")
    List<ItemCategory> findTopCategories();
    
    /**
     * 분류명으로 조회 (부분 일치)
     */
    @Query("SELECT ic FROM ItemCategory ic WHERE ic.categoryName LIKE %:categoryName% ORDER BY ic.parentsCategoryCode, ic.categoryLevel")
    List<ItemCategory> findByCategoryNameContaining(@Param("categoryName") String categoryName);
    
    /**
     * 셀렉트박스용 중분류 조회 (대분류명 포함)
     * 상위분류코드가 0이 아닌 중분류만 조회하고 대분류명과 함께 반환
     * @param hqCode 본사코드
     * @return 중분류코드, 대분류명, 중분류명
     */
    @Query("SELECT sub.categoryCode, major.categoryName, sub.categoryName " +
           "FROM ItemCategory sub " +
           "JOIN ItemCategory major ON sub.parentsCategoryCode = major.categoryCode " +
           "WHERE sub.parentsCategoryCode != 0 " +
           "AND (:hqCode IS NULL OR sub.hqCode = :hqCode) " +
           "ORDER BY major.categoryCode, sub.categoryCode")
    List<Object[]> findSubCategoriesWithMajorName(@Param("hqCode") Integer hqCode);
}