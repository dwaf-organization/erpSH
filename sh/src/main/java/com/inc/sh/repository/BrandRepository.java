package com.inc.sh.repository;
import com.inc.sh.entity.BrandInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<BrandInfo, Integer> {
    
    // 브랜드코드로 조회
    BrandInfo findByBrandCode(Integer brandCode);
    
    // 본사코드로 전체 조회
    List<BrandInfo> findByHqCodeOrderByBrandCodeDesc(Integer hqCode);
    
    // 본사코드와 브랜드코드로 조회
    Optional<BrandInfo> findByHqCodeAndBrandCode(Integer hqCode, Integer brandCode);
    
    // 브랜드 사용 여부 확인 (customer 테이블에서 참조 확인)
    @Query(value = "SELECT COUNT(*) FROM customer WHERE brand_code = :brandCode", nativeQuery = true)
    Long countCustomersByBrandCode(@Param("brandCode") Integer brandCode);
    
    // 브랜드 사용 여부 확인 (order_limit_set 테이블에서 참조 확인)
    @Query(value = "SELECT COUNT(*) FROM order_limit_set WHERE brand_code = :brandCode", nativeQuery = true)
    Long countOrderLimitSetByBrandCode(@Param("brandCode") Integer brandCode);
    
    /**
     * 관리자 - 브랜드 목록 조회 (본사명 포함)
     */
    @Query(value = "SELECT " +
           "b.brand_code, " +         // 0
           "b.brand_name, " +         // 1
           "b.hq_code, " +            // 2
           "h.company_name, " +       // 3
           "b.note " +                // 4
           "FROM brand_info b " +
           "LEFT JOIN headquarter h ON b.hq_code = h.hq_code " +
           "WHERE (:hqCode IS NULL OR b.hq_code = :hqCode) " +
           "ORDER BY b.brand_code", nativeQuery = true)
    List<Object[]> findBrandsForAdmin(@Param("hqCode") Integer hqCode);
    
}