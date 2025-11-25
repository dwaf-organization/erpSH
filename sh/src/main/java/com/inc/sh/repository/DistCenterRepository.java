package com.inc.sh.repository;

import com.inc.sh.entity.DistCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistCenterRepository extends JpaRepository<DistCenter, Integer> {
    
    /**
     * 물류센터코드로 조회
     */
    DistCenter findByDistCenterCode(Integer distCenterCode);
    
    /**
     * 물류센터코드 존재 여부 확인
     */
    boolean existsByDistCenterCode(Integer distCenterCode);
    
    /**
     * 검색 조건으로 물류센터 조회
     * @param distCenterCode 물류센터코드 (완전일치, null 가능)
     * @param useYn 사용여부 (완전일치, null 가능)
     * @return 조회된 물류센터 목록
     */
    @Query("SELECT dc FROM DistCenter dc WHERE " +
           "(:distCenterCode IS NULL OR dc.distCenterCode = :distCenterCode) AND " +
           "(:useYn IS NULL OR dc.useYn = :useYn) " +
           "ORDER BY dc.distCenterCode ASC")
    List<DistCenter> findBySearchConditions(
        @Param("distCenterCode") Integer distCenterCode,
        @Param("useYn") Integer useYn
    );
    
    /**
     * 사용중인 물류센터 조회
     */
    @Query("SELECT dc FROM DistCenter dc WHERE dc.useYn = 1 ORDER BY dc.distCenterCode ASC")
    List<DistCenter> findActiveDistCenters();
    
    /**
     * 본사별 물류센터 조회
     */
    List<DistCenter> findByHqCode(Integer hqCode);
}