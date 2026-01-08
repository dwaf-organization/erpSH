package com.inc.sh.repository;

import com.inc.sh.entity.OrderOptionPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderOptionPlatformRepository extends JpaRepository<OrderOptionPlatform, Integer> {
    
    /**
     * 주문상세 코드별 옵션 조회
     */
    List<OrderOptionPlatform> findByOrderDetailPlatformCode(Integer orderDetailPlatformCode);
    
    /**
     * 매장 플랫폼 코드별 옵션 조회
     */
    List<OrderOptionPlatform> findByStorePlatformCode(Integer storePlatformCode);
    
    /**
     * 거래처별 주문옵션 조회
     */
    List<OrderOptionPlatform> findByCustomerCode(Integer customerCode);
    
    /**
     * 브랜드별 주문옵션 조회
     */
    List<OrderOptionPlatform> findByBrandCode(Integer brandCode);
}