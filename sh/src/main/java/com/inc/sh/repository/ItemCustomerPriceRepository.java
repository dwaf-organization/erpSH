package com.inc.sh.repository;

import com.inc.sh.entity.ItemCustomerPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemCustomerPriceRepository extends JpaRepository<ItemCustomerPrice, Integer> {
    
    /**
     * 품목코드와 거래처코드로 조회
     */
    Optional<ItemCustomerPrice> findByItemCodeAndCustomerCode(Integer itemCode, Integer customerCode);
    
    /**
     * 품목코드별 거래처 단가 목록 조회
     */
    List<ItemCustomerPrice> findByItemCode(Integer itemCode);
    
    /**
     * 거래처코드별 품목 단가 목록 조회
     */
    List<ItemCustomerPrice> findByCustomerCode(Integer customerCode);
    
    /**
     * 품목코드와 거래처코드로 존재 여부 확인
     */
    boolean existsByItemCodeAndCustomerCode(Integer itemCode, Integer customerCode);
    
    /**
     * 품목코드와 거래처코드로 삭제
     */
    void deleteByItemCodeAndCustomerCode(Integer itemCode, Integer customerCode);
    
    /**
     * 활성 거래처 단가 조회 (종료일자가 없거나 미래인 것)
     * @param itemCode 품목코드
     * @return 활성 거래처 단가 목록
     */
    @Query("SELECT icp FROM ItemCustomerPrice icp WHERE icp.itemCode = :itemCode AND (icp.endDt IS NULL OR icp.endDt >= :currentDate)")
    List<ItemCustomerPrice> findActiveByItemCode(@Param("itemCode") Integer itemCode, @Param("currentDate") String currentDate);
}