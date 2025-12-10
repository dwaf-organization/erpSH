package com.inc.sh.repository;

import com.inc.sh.entity.OrderLimitSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLimitSetRepository extends JpaRepository<OrderLimitSet, Integer> {
    
    /**
     * 특정 brand_code에 해당하는 모든 주문 제한 설정을 조회
     * @param brandCode 조회할 브랜드 코드
     * @return 해당 브랜드의 OrderLimitSet 엔티티 리스트
     */
    List<OrderLimitSet> findByBrandCode(Integer brandCode);
    
    /**
     * 브랜드코드와 본사코드로 주문 제한 설정을 조회
     * @param brandCode 조회할 브랜드 코드
     * @param hqCode 조회할 본사 코드
     * @return 해당 브랜드 및 본사의 OrderLimitSet 엔티티 리스트
     */
    List<OrderLimitSet> findByBrandCodeAndHqCode(Integer brandCode, Integer hqCode);
    
    /**
     * 브랜드코드와 요일명으로 주문제한설정 조회 (주문시 체크용)
     */
    List<OrderLimitSet> findByBrandCodeAndDayName(Integer brandCode, String dayName);
}