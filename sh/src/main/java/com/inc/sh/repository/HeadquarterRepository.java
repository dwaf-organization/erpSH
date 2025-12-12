package com.inc.sh.repository;

import com.inc.sh.entity.Headquarter;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HeadquarterRepository extends JpaRepository<Headquarter, Integer> {
    
    /**
     * 본사코드로 본사 정보 조회
     */
    Headquarter findByHqCode(Integer hqCode);
    
    /**
     * 본사코드 존재 여부 확인
     */
    boolean existsByHqCode(Integer hqCode);
    
    /**
     * 사업자번호 중복 확인
     */
    boolean existsByBizNum(String bizNum);
    
    /**
     * 사업자번호로 본사 조회 (Optional)
     */
    java.util.Optional<Headquarter> findByBizNum(String bizNum);
    
    /**
     * 본사접속코드 중복 확인
     */
    boolean existsByHqAccessCode(String hqAccessCode);
    
    /**
     * 본사접속코드로 본사 조회
     */
    Headquarter findByHqAccessCode(@Param("hqAccessCode") String hqAccessCode);
    
    /**
     * 관리자 - 본사별 통계 조회 (JOIN + COUNT)
     */
    @Query(value = "SELECT " +
           "h.hq_code, " +
           "h.company_name, " +
           "h.biz_num, " +
           "COUNT(DISTINCT c.customer_code) as customerCount, " +
           "COUNT(DISTINCT i.item_code) as itemCount, " +
           "COUNT(DISTINCT b.brand_code) as brandCount, " +
           "COUNT(DISTINCT u.user_code) as userCount, " +
           "h.created_at " +
           "FROM headquarter h " +
           "LEFT JOIN customer c ON h.hq_code = c.hq_code " +
           "LEFT JOIN item i ON h.hq_code = i.hq_code " +
           "LEFT JOIN brand_info b ON h.hq_code = b.hq_code " +
           "LEFT JOIN user u ON h.hq_code = u.hq_code " +
           "GROUP BY h.hq_code, h.company_name, h.biz_num, h.created_at " +
           "ORDER BY h.hq_code DESC", nativeQuery = true)
    List<Object[]> findHeadquartersStatsForAdmin();
}