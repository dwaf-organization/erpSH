package com.inc.sh.repository;

import com.inc.sh.entity.CustomerCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerCartRepository extends JpaRepository<CustomerCart, Integer> {
    
    /**
     * 거래처사용자별 장바구니 조회
     */
    List<CustomerCart> findByCustomerCodeAndCustomerUserCode(
        Integer customerCode,
        Integer customerUserCode
    );
    
    /**
     * 장바구니 중복 확인 (같은 품목+창고가 이미 있는지)
     */
    boolean existsByCustomerCodeAndCustomerUserCodeAndItemCodeAndWarehouseCode(
        Integer customerCode,
        Integer customerUserCode,
        Integer itemCode,
        Integer warehouseCode
    );
}