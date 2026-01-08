package com.inc.sh.repository;

import com.inc.sh.entity.StoreRankingCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRankingCacheRepository extends JpaRepository<StoreRankingCache, Integer> {
    
    /**
     * 플랫폼별 순위 캐시 조회
     */
    List<StoreRankingCache> findByPlatformAndRankingTypeAndRankingPeriodAndBrandCodeOrderByRankNumber(
            String platform, String rankingType, String rankingPeriod, Integer brandCode);
    
    /**
     * 기존 순위 캐시 삭제
     */
    @Modifying
    @Query("DELETE FROM StoreRankingCache src WHERE src.platform = :platform AND src.rankingType = :rankingType AND src.rankingPeriod = :rankingPeriod AND src.brandCode = :brandCode")
    void deleteByPlatformAndRankingTypeAndRankingPeriodAndBrandCode(
            @Param("platform") String platform, 
            @Param("rankingType") String rankingType, 
            @Param("rankingPeriod") String rankingPeriod,
            @Param("brandCode") Integer brandCode);
    
    /**
     * 브랜드별 순위 캐시 조회
     */
    List<StoreRankingCache> findByBrandCodeOrderByPlatformAscRankNumberAsc(Integer brandCode);
    
    /**
     * 특정 매장의 순위 조회
     */
    List<StoreRankingCache> findByStorePlatformCodeAndBrandCode(Integer storePlatformCode, Integer brandCode);
}