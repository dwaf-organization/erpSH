package com.inc.sh.repository;

import com.inc.sh.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerUserRepository extends JpaRepository<CustomerUser, Integer> {
    
    /**
     * 사용자코드로 거래처사용자 조회
     */
    CustomerUser findByCustomerUserCode(@Param("customerUserCode") Integer customerUserCode);
    
    /**
     * 사용자코드 존재 여부 확인
     */
    boolean existsByCustomerUserCode(@Param("customerUserCode") Integer customerUserCode);
    
    /**
     * 거래처사용자 검색 조회 (거래처명 포함)
     */
    @Query(value = "SELECT " +
           "cu.customer_code, " +
           "c.customer_name, " +
           "cu.customer_user_code, " +
           "cu.customer_user_id, " +
           "cu.customer_user_name, " +
           "cu.contact_num, " +
           "cu.email, " +
           "cu.end_yn, " +
           "cu.customer_user_pw " +
           "FROM customer_user cu " +
           "LEFT JOIN customer c ON cu.customer_code = c.customer_code " +
           "WHERE (:customerCode IS NULL OR cu.customer_code = :customerCode) " +
           "AND (:customerUserId IS NULL OR cu.customer_user_id LIKE CONCAT('%', :customerUserId, '%')) " +
           "ORDER BY cu.customer_user_code", nativeQuery = true)
    List<Object[]> findCustomerUsersWithCustomerByConditions(
        @Param("customerCode") Integer customerCode,
        @Param("customerUserId") String customerUserId
    );
    
    /**
     * 거래처코드로 거래처사용자 조회
     */
    List<CustomerUser> findByCustomerCodeOrderByCustomerUserCode(@Param("customerCode") Integer customerCode);
    
    /**
     * 거래처사용자 아이디 존재 여부 확인
     */
    boolean existsByCustomerUserId(@Param("customerUserId") String customerUserId);
    
    /**
     * 거래처사용자 아이디로 조회
     */
    CustomerUser findByCustomerUserId(@Param("customerUserId") String customerUserId);
    
    /**
     * 거래처별 가상계좌코드 조회 (거래처사용자 등록 시 사용)
     */
    @Query(value = "SELECT c.virtual_account FROM customer c WHERE c.customer_code = :customerCode", nativeQuery = true)
    String findVirtualAccountByCustomerCode(@Param("customerCode") Integer customerCode);
    
    /**
     * ✅ 거래처사용자의 장바구니 데이터 개수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM customer_cart " +
           "WHERE customer_code = :customerCode " +
           "AND customer_user_code = :customerUserCode", nativeQuery = true)
    Long countCartByCustomerUser(@Param("customerUserCode") Integer customerUserCode, 
                                @Param("customerCode") Integer customerCode);
    
    /**
     * ✅ 거래처사용자의 위시리스트 데이터 개수 조회
     */
    @Query(value = "SELECT COUNT(*) FROM customer_wishlist " +
           "WHERE customer_code = :customerCode " +
           "AND customer_user_code = :customerUserCode", nativeQuery = true)
    Long countWishlistByCustomerUser(@Param("customerUserCode") Integer customerUserCode, 
                                    @Param("customerCode") Integer customerCode);
    
    /**
     * ✅ 거래처사용자의 주문내역 개수 조회 (필요시 사용)
     */
    @Query(value = "SELECT COUNT(*) FROM order_table o " +
           "JOIN customer_user cu ON o.customer_code = cu.customer_code " +
           "WHERE cu.customer_user_code = :customerUserCode", nativeQuery = true)
    Long countOrdersByCustomerUser(@Param("customerUserCode") Integer customerUserCode);
}