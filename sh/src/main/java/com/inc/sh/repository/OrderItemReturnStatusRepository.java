package com.inc.sh.repository;

import com.inc.sh.entity.OrderItemReturnStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemReturnStatusRepository extends JpaRepository<OrderItemReturnStatus, Integer> {
    
    /**
     * 거래처별 반품가능 주문품목 조회 (앱용)
     */
    List<OrderItemReturnStatus> findByCustomerCodeOrderByOrderNoDesc(Integer customerCode);
    
    /**
     * 특정 주문의 반품가능 주문품목 조회 (앱용)
     */
    List<OrderItemReturnStatus> findByOrderNoAndCustomerCodeOrderByItemCode(String orderNo, Integer customerCode);
    
    /**
     * ERP용 반품가능 주문품목 조회 (거래처코드, 주문번호 조건부)
     */
    @Query("SELECT oirs FROM OrderItemReturnStatus oirs " +
           "WHERE (:customerCode IS NULL OR oirs.customerCode = :customerCode) " +
           "AND (:orderNo IS NULL OR oirs.orderNo = :orderNo) " +
           "ORDER BY oirs.orderNo DESC, oirs.itemCode")
    List<OrderItemReturnStatus> findReturnableItems(
        @Param("customerCode") Integer customerCode,
        @Param("orderNo") String orderNo
    );
    
    /**
     * 반품가능한 주문번호 목록 조회 (앱용)
     */
    @Query("SELECT DISTINCT oirs.orderNo FROM OrderItemReturnStatus oirs " +
           "WHERE oirs.customerCode = :customerCode " +
           "ORDER BY oirs.orderNo DESC")
    List<String> findAvailableOrderNumbers(@Param("customerCode") Integer customerCode);
}