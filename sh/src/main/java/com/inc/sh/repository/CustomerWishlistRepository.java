package com.inc.sh.repository;

import com.inc.sh.entity.CustomerWishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerWishlistRepository extends JpaRepository<CustomerWishlist, Integer> {

    /**
     * 위시리스트 여부 확인
     */
    boolean existsByCustomerCodeAndCustomerUserCodeAndItemCode(
        @Param("customerCode") Integer customerCode,
        @Param("customerUserCode") Integer customerUserCode,
        @Param("itemCode") Integer itemCode
    );

    /**
     * 거래처별 위시리스트 품목 조회
     */
    List<CustomerWishlist> findByCustomerCodeAndCustomerUserCode(
        @Param("customerCode") Integer customerCode,
        @Param("customerUserCode") Integer customerUserCode
    );

    /**
     * [앱전용] 위시리스트 품목코드 목록 조회
     */
    @Query("SELECT cw.itemCode FROM CustomerWishlist cw " +
           "WHERE cw.customerCode = :customerCode " +
           "AND cw.customerUserCode = :customerUserCode")
    List<Integer> findItemCodesByCustomerCodeAndCustomerUserCode(
        @Param("customerCode") Integer customerCode,
        @Param("customerUserCode") Integer customerUserCode
    );

}