package com.inc.sh.repository;
import com.inc.sh.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    /**
     * 주문품목코드로 조회
     */
    OrderItem findByOrderItemCode(Integer orderItemCode);
    
    /**
     * 주문번호별 품목 조회
     */
    List<OrderItem> findByOrderNo(String orderNo);
    
    /**
     * 주문번호별 품목 삭제
     */
    void deleteByOrderNo(String orderNo);
    
    /**
     * 품목별 주문품목 조회
     */
    List<OrderItem> findByItemCode(Integer itemCode);
    
    /**
     * 창고별 주문품목 조회
     */
    List<OrderItem> findByReleaseWarehouseCode(Integer releaseWarehouseCode);
    
    /**
     * 주문별 금액 합계 조회
     */
    @Query("SELECT " +
           "COALESCE(SUM(oi.taxableAmt), 0), " +
           "COALESCE(SUM(oi.taxFreeAmt), 0), " +
           "COALESCE(SUM(oi.supplyAmt), 0), " +
           "COALESCE(SUM(oi.vatAmt), 0), " +
           "COALESCE(SUM(oi.totalAmt), 0), " +
           "COALESCE(SUM(oi.totalQty), 0) " +
           "FROM OrderItem oi WHERE oi.orderNo = :orderNo")
    Object[] findOrderSummaryByOrderNo(@Param("orderNo") String orderNo);
    
    /**
     * 반품등록용 - 주문품목 조회 (거래처명, 창고명 JOIN)
     */
    @Query(value = "SELECT " +
           "o.customer_code, " +            // 0 - order 테이블에서 가져옴
           "c.customer_name, " +            // 1
           "oi.order_no, " +                // 2
           "oi.item_code, " +               // 3
           "oi.item_name, " +               // 4
           "oi.specification, " +           // 5
           "oi.unit, " +                    // 6
           "oi.price_type, " +              // 7
           "oi.order_unit_price, " +        // 8 - unit_price → order_unit_price로 수정
           "oi.order_qty, " +               // 9 - total_qty → order_qty로 수정 
           "oi.tax_target, " +              // 10
           "oi.release_warehouse_code, " +  // 11
           "w.warehouse_name, " +           // 12
           "oi.taxable_amt, " +             // 13
           "oi.tax_free_amt, " +            // 14
           "oi.supply_amt, " +              // 15
           "oi.vat_amt, " +                 // 16
           "oi.total_amt " +                // 17
           "FROM order_item oi " +
           "LEFT JOIN `order` o ON oi.order_no = o.order_no " +           // order 테이블 JOIN 추가
           "LEFT JOIN customer c ON o.customer_code = c.customer_code " + // order를 통해 customer JOIN
           "LEFT JOIN warehouse w ON oi.release_warehouse_code = w.warehouse_code " +
           "WHERE (:customerCode IS NULL OR o.customer_code = :customerCode) " +     // order 테이블의 customer_code 사용
           "AND (:orderNo IS NULL OR oi.order_no = :orderNo) " +
           "ORDER BY oi.order_no, oi.item_code", nativeQuery = true)
    List<Object[]> findOrderItemsForReturn(
        @Param("customerCode") Integer customerCode,
        @Param("orderNo") String orderNo
    );
    
	 /**
	  * 반품 가능한 주문품목 조회 (배송완료 + 반품가능수량 > 0)
	  */
	 @Query(value = "SELECT oi.* FROM order_item oi " +
	    "JOIN `order` o ON oi.order_no = o.order_no " +
	    "WHERE o.delivery_status = '배송완료' " +
	    "AND (oi.order_qty - oi.returned_qty) > 0 " +
	    "AND o.order_dt >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 30 DAY), '%Y%m%d') " +
	    "AND (:customerCode IS NULL OR o.customer_code = :customerCode) " +
	    "AND (:orderNo IS NULL OR oi.order_no = :orderNo) " +
	    "ORDER BY o.order_dt DESC, oi.order_item_code", nativeQuery = true)
	 List<OrderItem> findReturnableOrderItems(
	     @Param("customerCode") Integer customerCode,
	 @Param("orderNo") String orderNo
	 );
	
	 /**
	  * 주문품목코드로 반품가능수량 포함 조회
	  */
	 @Query("SELECT oi, (oi.orderQty - oi.returnedQty) as availableQty " +
	    "FROM OrderItem oi WHERE oi.orderItemCode = :orderItemCode")
	 Object[] findOrderItemWithAvailableQty(@Param("orderItemCode") Integer orderItemCode);
	
	 /**
	  * 반품된수량 업데이트
	  */
	 @Modifying
	 @Query("UPDATE OrderItem oi SET oi.returnedQty = oi.returnedQty + :returnQty " +
	    "WHERE oi.orderItemCode = :orderItemCode")
	 void updateReturnedQty(@Param("orderItemCode") Integer orderItemCode, @Param("returnQty") Integer returnQty);
}