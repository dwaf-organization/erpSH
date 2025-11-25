package com.inc.sh.repository;

import com.inc.sh.entity.OrderLimitCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLimitCustomerRepository extends JpaRepository<OrderLimitCustomer, Integer> {
    
    /**
     * 품목코드로 제한된 거래처 목록 조회
     */
    List<OrderLimitCustomer> findByItemCode(Integer itemCode);
    
    /**
     * 품목코드별 제한된 거래처 코드 목록 조회
     */
    @Query("SELECT olc.customerCode FROM OrderLimitCustomer olc WHERE olc.itemCode = :itemCode")
    List<Integer> findCustomerCodesByItemCode(@Param("itemCode") Integer itemCode);
    
    /**
     * 품목코드와 거래처코드로 삭제
     */
    void deleteByItemCodeAndCustomerCode(Integer itemCode, Integer customerCode);
    
    /**
     * 품목코드와 거래처코드로 제한 여부 확인
     */
    boolean existsByItemCodeAndCustomerCode(Integer itemCode, Integer customerCode);
}