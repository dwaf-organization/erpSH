package com.inc.sh.repository;

import com.inc.sh.entity.OrderDetailPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailPlatformRepository extends JpaRepository<OrderDetailPlatform, Integer> {
    
    /**
     * 주문 플랫폼 코드별 상세 조회
     */
    List<OrderDetailPlatform> findByOrderPlatformCode(Integer orderPlatformCode);
    
    /**
     * 매장 플랫폼 코드별 상세 조회
     */
    List<OrderDetailPlatform> findByStorePlatformCode(Integer storePlatformCode);
    
    /**
     * 거래처별 주문상세 조회
     */
    List<OrderDetailPlatform> findByCustomerCode(Integer customerCode);
    
    /**
     * 브랜드별 주문상세 조회
     */
    List<OrderDetailPlatform> findByBrandCode(Integer brandCode);
}