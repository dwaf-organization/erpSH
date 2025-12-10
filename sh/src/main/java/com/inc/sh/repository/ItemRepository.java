package com.inc.sh.repository;

import com.inc.sh.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {
    
    /**
     * 품목코드로 품목 조회
     */
    Item findByItemCode(Integer itemCode);
    
    /**
     * 품목코드 존재 여부 확인
     */
    boolean existsByItemCode(Integer itemCode);
    
    /**
     * 복합 조건으로 품목 조회
     * @param itemCode 품목코드 (부분일치, null 가능)
     * @param itemName 품명 (부분일치, null 가능)
     * @param categoryCode 대분류코드 (완전일치, null 가능)
     * @param priceType 단가설정 (완전일치, null 가능)
     * @param endDtYn 종료여부 (1=종료, 0=미종료, null=전체)
     * @param orderAvailableYn 주문가능여부 (완전일치, null 가능)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:itemCode IS NULL OR CAST(i.itemCode AS string) LIKE %:itemCode%) AND " +
           "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:priceType IS NULL OR i.priceType = :priceType) AND " +
           "(:endDtYn IS NULL OR " +
           "  (CASE WHEN :endDtYn = 1 THEN i.endDt IS NOT NULL " +
           "        WHEN :endDtYn = 0 THEN i.endDt IS NULL END)) AND " +
           "(:orderAvailableYn IS NULL OR i.orderAvailableYn = :orderAvailableYn) " +
           "ORDER BY i.itemCode DESC")
    List<Item> findBySearchConditions(
        @Param("itemCode") String itemCode,
        @Param("itemName") String itemName,
        @Param("categoryCode") Integer categoryCode,
        @Param("priceType") Integer priceType,
        @Param("endDtYn") Integer endDtYn,
        @Param("orderAvailableYn") Integer orderAvailableYn
    );
    
    /**
     * 분류별 품목 조회
     */
    List<Item> findByCategoryCode(Integer categoryCode);
    
    /**
     * 분류별 품목코드 목록 조회 (분류 삭제시 사용)
     */
    @Query("SELECT i.itemCode FROM Item i WHERE i.categoryCode = :categoryCode")
    List<Integer> findItemCodesByCategoryCode(@Param("categoryCode") Integer categoryCode);
    
    /**
     * 활성 품목 조회 (종료일자가 없는 것)
     */
    @Query("SELECT i FROM Item i WHERE i.endDt IS NULL ORDER BY i.itemCode DESC")
    List<Item> findActiveItems();
    
    /**
     * 주문 가능한 품목 조회
     */
    @Query("SELECT i FROM Item i WHERE i.orderAvailableYn IN (1, 2) AND i.endDt IS NULL ORDER BY i.itemCode DESC")
    List<Item> findOrderableItems();
    
    /**
     * 주문제한 설정용 품목 조회
     * @param categoryCode 품목분류코드 (완전일치, null 가능)
     * @param itemName 품명 (부분일치, null 가능)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForOrderLimitManagement(
        @Param("categoryCode") Integer categoryCode,
        @Param("itemName") String itemName
    );
    
    /**
     * 주문제한 설정용 품목 조회 (본사별)
     * @param categoryCode 품목분류코드 (완전일치, null 가능)
     * @param itemName 품명 (부분일치, null 가능)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
           "i.hqCode = :hqCode AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForOrderLimitManagementWithHqCode(
        @Param("categoryCode") Integer categoryCode,
        @Param("itemName") String itemName,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 거래처별 단가관리용 품목 조회
     * @param categoryCode 분류코드 (완전일치, null 가능)
     * @param itemName 품명 (부분일치, null 가능)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForItemCustomerPriceManagement(
        @Param("categoryCode") Integer categoryCode,
        @Param("itemName") String itemName
    );
    
    /**
     * 거래처별 단가관리용 품목 조회 (본사별)
     * @param categoryCode 분류코드 (완전일치, null 가능)
     * @param itemName 품명 (부분일치, null 가능)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:itemName IS NULL OR i.itemName LIKE %:itemName%) AND " +
           "i.hqCode = :hqCode AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForItemCustomerPriceManagementWithHqCode(
        @Param("categoryCode") Integer categoryCode,
        @Param("itemName") String itemName,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 납품단가관리용 품목 조회
     * @param itemCode 품목코드 (완전일치, null 가능)
     * @param categoryCode 분류코드 (완전일치, null 가능)
     * @param priceType 단가유형 (완전일치, null 가능)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:itemCode IS NULL OR i.itemCode = :itemCode) AND " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:priceType IS NULL OR i.priceType = :priceType) AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForDeliveryPriceManagement(
        @Param("itemCode") Integer itemCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("priceType") Integer priceType
    );
    
    /**
     * 납품단가관리용 품목 조회 (본사별)
     * @param itemCode 품목코드 (완전일치, null 가능)
     * @param categoryCode 분류코드 (완전일치, null 가능)
     * @param priceType 단가유형 (완전일치, null 가능)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
           "(:itemCode IS NULL OR i.itemCode = :itemCode) AND " +
           "(:categoryCode IS NULL OR i.categoryCode = :categoryCode) AND " +
           "(:priceType IS NULL OR i.priceType = :priceType) AND " +
           "i.hqCode = :hqCode AND " +
           "i.endDt IS NULL " + // 활성 품목만
           "ORDER BY i.itemCode ASC")
    List<Item> findForDeliveryPriceManagementWithHqCode(
        @Param("itemCode") Integer itemCode,
        @Param("categoryCode") Integer categoryCode,
        @Param("priceType") Integer priceType,
        @Param("hqCode") Integer hqCode
    );
    
    /**
     * 팝업용 품목 검색 (수정됨)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @param item 품목코드/품명 (부분일치, null 가능)
     * @param categoryCode 분류코드 (완전일치, null 가능)
     * @param priceType 단가유형 (완전일치, null 가능)
     * @return 조회된 품목 목록
     */
    @Query(value = "SELECT * FROM item i " +
           "WHERE i.hq_code = :hqCode " +
           "AND (:item IS NULL OR " +
           "     CAST(i.item_code AS CHAR) LIKE CONCAT('%', :item, '%') OR " +
           "     i.item_name LIKE CONCAT('%', :item, '%')) " +
           "AND (:categoryCode IS NULL OR i.category_code = :categoryCode) " +
           "AND (:priceType IS NULL OR i.price_type = :priceType) " +
           "AND i.order_available_yn = 1 " +  // 주문가능 품목만
           "ORDER BY i.item_code ASC", nativeQuery = true)
    List<Item> findByPopupSearchConditions(
        @Param("hqCode") Integer hqCode,
        @Param("item") String item,
        @Param("categoryCode") Integer categoryCode,
        @Param("priceType") Integer priceType
    );
    
    /**
     * [앱전용] 주문가능품목 조회 - 전체
     */
    @Query(value = "SELECT DISTINCT " +
           "i.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "i.vat_type, " +
           "i.vat_detail, " +
           "i.category_code, " +
           "i.origin, " +
           "i.price_type, " +
           "COALESCE(icp.customer_supply_price, i.base_price) as base_price, " +
           "COALESCE(icp.supply_price, i.supply_price) as supply_price, " +
           "COALESCE(icp.tax_amount, i.tax_amount) as tax_amount, " +
           "COALESCE(icp.taxable_amount, i.taxable_amount) as taxable_amount, " +
           "COALESCE(icp.duty_free_amount, i.duty_free_amount) as duty_free_amount, " +
           "COALESCE(icp.total_amount, i.total_amount) as total_amount, " +
           "i.order_available_yn, " +
           "i.min_order_qty, " +
           "i.max_order_qty, " +
           "i.deadline_day, " +
           "i.deadline_time, " +
           "wi.current_quantity, " +
           "wi.warehouse_code " +
           "FROM item i " +
           "JOIN warehouse_items wi ON i.item_code = wi.item_code " +
           "JOIN warehouse w ON wi.warehouse_code = w.warehouse_code " +
           "JOIN customer c ON w.dist_center_code = c.dist_center_code " +
           "LEFT JOIN item_customer_price icp ON i.item_code = icp.item_code AND icp.customer_code = :customerCode " +
           "LEFT JOIN order_limit_customer olc ON i.item_code = olc.item_code AND olc.customer_code = :customerCode " +
           "WHERE c.customer_code = :customerCode " +
           "AND i.order_available_yn != 0 " +
           "AND (i.order_available_yn = 1 OR (i.order_available_yn = 2 AND olc.item_code IS NULL)) " +
           "AND (:categoryCode IS NULL OR :categoryCode = '전체' OR i.category_code = :categoryCode) " +
           "AND (:itemName IS NULL OR i.item_name LIKE CONCAT('%', :itemName, '%')) " +
           "ORDER BY i.item_code", nativeQuery = true)
    List<Object[]> findAllOrderableItemsForApp(
        @Param("customerCode") Integer customerCode,
        @Param("categoryCode") String categoryCode,
        @Param("itemName") String itemName
    );

    /**
     * [앱전용] 주문가능품목 조회 - 위시리스트
     */
    @Query(value = "SELECT DISTINCT " +
           "i.item_code, " +
           "i.item_name, " +
           "i.specification, " +
           "i.purchase_unit, " +
           "i.vat_type, " +
           "i.vat_detail, " +
           "i.category_code, " +
           "i.origin, " +
           "i.price_type, " +
           "COALESCE(icp.customer_supply_price, i.base_price) as base_price, " +
           "COALESCE(icp.supply_price, i.supply_price) as supply_price, " +
           "COALESCE(icp.tax_amount, i.tax_amount) as tax_amount, " +
           "COALESCE(icp.taxable_amount, i.taxable_amount) as taxable_amount, " +
           "COALESCE(icp.duty_free_amount, i.duty_free_amount) as duty_free_amount, " +
           "COALESCE(icp.total_amount, i.total_amount) as total_amount, " +
           "i.order_available_yn, " +
           "i.min_order_qty, " +
           "i.max_order_qty, " +
           "i.deadline_day, " +
           "i.deadline_time, " +
           "wi.current_quantity, " +
           "wi.warehouse_code " +
           "FROM item i " +
           "JOIN warehouse_items wi ON i.item_code = wi.item_code " +
           "JOIN warehouse w ON wi.warehouse_code = w.warehouse_code " +
           "JOIN customer c ON w.dist_center_code = c.dist_center_code " +
           "JOIN customer_wishlist cw ON i.item_code = cw.item_code AND cw.customer_code = :customerCode AND cw.customer_user_code = :customerUserCode " +
           "LEFT JOIN item_customer_price icp ON i.item_code = icp.item_code AND icp.customer_code = :customerCode " +
           "LEFT JOIN order_limit_customer olc ON i.item_code = olc.item_code AND olc.customer_code = :customerCode " +
           "WHERE c.customer_code = :customerCode " +
           "AND i.order_available_yn != 0 " +
           "AND (i.order_available_yn = 1 OR (i.order_available_yn = 2 AND olc.item_code IS NULL)) " +
           "AND (:categoryCode IS NULL OR :categoryCode = '전체' OR i.category_code = :categoryCode) " +
           "AND (:itemName IS NULL OR i.item_name LIKE CONCAT('%', :itemName, '%')) " +
           "ORDER BY i.item_code", nativeQuery = true)
    List<Object[]> findWishlistOrderableItemsForApp(
        @Param("customerCode") Integer customerCode,
        @Param("customerUserCode") Integer customerUserCode,
        @Param("categoryCode") String categoryCode,
        @Param("itemName") String itemName
    );
    
    /**
     * 복합 조건으로 품목 조회 (Entity 구조에 맞춰 수정)
     * @param item 품목코드 또는 품목명 (부분검색, null/빈값 가능)
     * @param categoryCode 분류코드 (null/빈값 가능)
     * @param priceType 단가유형 (null/빈값/1/2)
     * @param endDtYn 종료여부 (null/빈값/0/1)
     * @param orderAvailableYn 주문가능여부 (null/빈값/0/1/2)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
	       "(:item IS NULL OR :item = '' OR " +
	       " STR(i.itemCode) LIKE CONCAT('%', :item, '%') OR " +
	       " i.itemName LIKE CONCAT('%', :item, '%')) AND " +
           "(:categoryCode IS NULL OR :categoryCode = '' OR " +
           " i.categoryCode = CAST(:categoryCode AS integer)) AND " +
           "(:priceType IS NULL OR :priceType = '' OR " +
           " i.priceType = CAST(:priceType AS integer)) AND " +
           "(:endDtYn IS NULL OR :endDtYn = '' OR " +
           " (CASE WHEN :endDtYn = '1' THEN i.endDt IS NOT NULL " +
           "       WHEN :endDtYn = '0' THEN i.endDt IS NULL END)) AND " +
           "(:orderAvailableYn IS NULL OR :orderAvailableYn = '' OR " +
           " i.orderAvailableYn = CAST(:orderAvailableYn AS integer)) " +
           "ORDER BY i.itemCode DESC")
    List<Item> findBySearchConditionsUpdated(
        @Param("item") String item,
        @Param("categoryCode") String categoryCode,
        @Param("priceType") String priceType,
        @Param("endDtYn") String endDtYn,
        @Param("orderAvailableYn") String orderAvailableYn
    );
    
    /**
     * 복합 조건으로 품목 조회 (본사별)
     * @param item 품목코드 또는 품목명 (부분검색, null/빈값 가능)
     * @param categoryCode 분류코드 (null/빈값 가능)
     * @param priceType 단가유형 (null/빈값/1/2)
     * @param endDtYn 종료여부 (null/빈값/0/1)
     * @param orderAvailableYn 주문가능여부 (null/빈값/0/1/2)
     * @param hqCode 본사코드 (완전일치, 필수)
     * @return 조회된 품목 목록
     */
    @Query("SELECT i FROM Item i WHERE " +
	       "(:item IS NULL OR :item = '' OR " +
	       " STR(i.itemCode) LIKE CONCAT('%', :item, '%') OR " +
	       " i.itemName LIKE CONCAT('%', :item, '%')) AND " +
           "(:categoryCode IS NULL OR :categoryCode = '' OR " +
           " i.categoryCode = CAST(:categoryCode AS integer)) AND " +
           "(:priceType IS NULL OR :priceType = '' OR " +
           " i.priceType = CAST(:priceType AS integer)) AND " +
           "(:endDtYn IS NULL OR :endDtYn = '' OR " +
           " (CASE WHEN :endDtYn = '1' THEN i.endDt IS NOT NULL " +
           "       WHEN :endDtYn = '0' THEN i.endDt IS NULL END)) AND " +
           "(:orderAvailableYn IS NULL OR :orderAvailableYn = '' OR " +
           " i.orderAvailableYn = CAST(:orderAvailableYn AS integer)) AND " +
           "i.hqCode = :hqCode " +
           "ORDER BY i.itemCode DESC")
    List<Item> findBySearchConditionsUpdatedWithHqCode(
        @Param("item") String item,
        @Param("categoryCode") String categoryCode,
        @Param("priceType") String priceType,
        @Param("endDtYn") String endDtYn,
        @Param("orderAvailableYn") String orderAvailableYn,
        @Param("hqCode") Integer hqCode
    );
}