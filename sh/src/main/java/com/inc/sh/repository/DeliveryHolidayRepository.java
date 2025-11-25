package com.inc.sh.repository;

import com.inc.sh.entity.DeliveryHoliday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryHolidayRepository extends JpaRepository<DeliveryHoliday, Integer> {

    /**
     * 배송휴일코드로 조회
     */
    DeliveryHoliday findByDeliveryHolidayCode(Integer deliveryHolidayCode);
    
    /**
     * 배송휴일코드 존재 여부 확인
     */
    boolean existsByDeliveryHolidayCode(Integer deliveryHolidayCode);
    
    /**
     * 브랜드별 배송휴일 조회 (브랜드명 포함)
     * @param brandCode 브랜드코드 (null 가능)
     * @return 조회된 배송휴일 목록 (브랜드명, 휴일, 요일 포함)
     */
    @Query("SELECT dh.deliveryHolidayCode, b.brandName, dh.holidayDt, dh.weekday, dh.holidayName " +
           "FROM DeliveryHoliday dh LEFT JOIN BrandInfo b ON dh.brandCode = b.brandCode " +
           "WHERE (:brandCode IS NULL OR dh.brandCode = :brandCode) " +
           "ORDER BY dh.brandCode ASC, dh.holidayDt ASC")
    List<Object[]> findHolidaysWithBrandName(@Param("brandCode") Integer brandCode);
    
    /**
     * 브랜드별 배송휴일 조회
     */
    List<DeliveryHoliday> findByBrandCode(Integer brandCode);
    
    /**
     * 본사별 배송휴일 조회
     */
    List<DeliveryHoliday> findByHqCode(Integer hqCode);
    
    /**
     * 특정 날짜와 브랜드코드로 배송휴일 조회 (주문시 체크용)
     */
    List<DeliveryHoliday> findByBrandCodeAndHolidayDt(Integer brandCode, String holidayDt);
}