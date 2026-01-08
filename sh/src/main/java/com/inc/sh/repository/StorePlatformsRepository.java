package com.inc.sh.repository;

import com.inc.sh.entity.StorePlatforms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorePlatformsRepository extends JpaRepository<StorePlatforms, Integer> {
    
    /**
     * 플랫폼별 활성화된 매장 정보 조회 (본사코드 포함)
     */
    List<StorePlatforms> findByPlatformAndHqCodeAndIsActiveTrue(String platform, Integer hqCode);
    
    /**
     * 플랫폼별 활성화된 매장 정보 조회
     */
    List<StorePlatforms> findByPlatformAndIsActiveTrue(String platform);
    
    /**
     * 거래처별 플랫폼 매장 정보 조회
     */
    List<StorePlatforms> findByCustomerCodeAndIsActiveTrue(Integer customerCode);
    
    /**
     * 브랜드별 플랫폼 매장 정보 조회
     */
    List<StorePlatforms> findByBrandCodeAndIsActiveTrue(Integer brandCode);
    
    /**
     * 특정 플랫폼의 특정 거래처 매장 조회
     */
    StorePlatforms findByCustomerCodeAndPlatformAndIsActiveTrue(Integer customerCode, String platform);
    
    /**
     * 동기화 시간 업데이트
     */
    @Modifying
    @Query("UPDATE StorePlatforms sp SET sp.lastSyncedAt = :syncTime WHERE sp.storePlatformCode = :storePlatformCode")
    void updateLastSyncedAt(@Param("storePlatformCode") Integer storePlatformCode, @Param("syncTime") String syncTime);
}