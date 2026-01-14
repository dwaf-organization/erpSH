package com.inc.sh.repository;

import com.inc.sh.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DeliveryAppConnectionRepository extends JpaRepository<Customer, Integer> {

    /**
     * 배달앱 연결 상태 조회 (페이징)
     *
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @param offset 시작 위치
     * @param limit 조회 개수
     * @return 배달앱 연결 정보 목록
     */
    @Query(value =
        "SELECT " +
        "    c.customer_code, " +
        "    c.customer_name, " +
        "    c.biz_num, " +
        "    (SELECT CASE WHEN COUNT(*) > 0 THEN '등록완료' ELSE '미등록' END " +
        "     FROM store_platforms sp " +
        "     WHERE sp.customer_code = c.customer_code " +
        "       AND sp.platform = '배민' " +
        "       AND sp.platform_store_id IS NOT NULL AND TRIM(sp.platform_store_id) != '' " +
        "       AND sp.login_id IS NOT NULL AND TRIM(sp.login_id) != '' " +
        "       AND sp.login_password IS NOT NULL AND TRIM(sp.login_password) != '') as baemin_status, " +
        "    (SELECT CASE WHEN COUNT(*) > 0 THEN '등록완료' ELSE '미등록' END " +
        "     FROM store_platforms sp " +
        "     WHERE sp.customer_code = c.customer_code " +
        "       AND sp.platform = '요기요' " +
        "       AND sp.platform_store_id IS NOT NULL AND TRIM(sp.platform_store_id) != '' " +
        "       AND sp.login_id IS NOT NULL AND TRIM(sp.login_id) != '' " +
        "       AND sp.login_password IS NOT NULL AND TRIM(sp.login_password) != '') as yogiyo_status, " +
        "    (SELECT CASE WHEN COUNT(*) > 0 THEN '등록완료' ELSE '미등록' END " +
        "     FROM store_platforms sp " +
        "     WHERE sp.customer_code = c.customer_code " +
        "       AND sp.platform = '쿠팡이츠' " +
        "       AND sp.platform_store_id IS NOT NULL AND TRIM(sp.platform_store_id) != '' " +
        "       AND sp.login_id IS NOT NULL AND TRIM(sp.login_id) != '' " +
        "       AND sp.login_password IS NOT NULL AND TRIM(sp.login_password) != '') as coupang_status " +
        "FROM customer c " +
        "WHERE c.hq_code = :hqCode " +
        "  AND (:brandCode = 0 OR c.brand_code = :brandCode) " +
        "  AND c.close_dt IS NULL " +
        "ORDER BY c.customer_code ",
        nativeQuery = true)
    List<Map<String, Object>> findDeliveryAppConnections(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode
    );

    /**
     * 배달앱 연결 대상 거래처 총 개수 조회
     *
     * @param hqCode 거래처코드
     * @param brandCode 브랜드코드 (0: 전체)
     * @return 총 거래처 개수
     */
    @Query(value =
        "SELECT COUNT(DISTINCT c.customer_code) " +
        "FROM customer c " +
        "WHERE c.hq_code = :hqCode " +
        "  AND (:brandCode = 0 OR c.brand_code = :brandCode) " +
        "  AND c.close_dt IS NULL ",
        nativeQuery = true)
    Long countDeliveryAppConnections(
        @Param("hqCode") Integer hqCode,
        @Param("brandCode") Integer brandCode
    );
    
    /**
     * 특정 거래처의 배달앱 정보 조회
     * 
     * @param hqCode 거래처코드 (본사코드)
     * @param customerCode 거래처코드
     * @return 플랫폼별 상세 정보
     */
    @Query(value = 
        "SELECT " +
        "    c.customer_code, " +
        "    c.customer_name, " +
        "    sp.platform, " +
        "    sp.platform_store_id, " +
        "    sp.login_id, " +
        "    sp.login_password " +
        "FROM customer c " +
        "LEFT JOIN store_platforms sp ON c.customer_code = sp.customer_code " +
        "WHERE c.hq_code = :hqCode " +
        "  AND c.customer_code = :customerCode " +
        "  AND c.close_dt IS NULL " +
        "ORDER BY sp.platform",
        nativeQuery = true)
    List<Map<String, Object>> findDeliveryAppInfo(
        @Param("hqCode") Integer hqCode,
        @Param("customerCode") Integer customerCode
    );
    
    /**
     * 특정 거래처의 특정 플랫폼 정보 조회 (저장용)
     * 
     * @param customerCode 거래처코드
     * @param platform 플랫폼
     * @return 기존 데이터 (수정용)
     */
    @Query(value = 
        "SELECT store_platform_code, platform_store_id, login_id, login_password " +
        "FROM store_platforms " +
        "WHERE customer_code = :customerCode " +
        "  AND platform = :platform " +
        "LIMIT 1",
        nativeQuery = true)
    Map<String, Object> findByCustomerCodeAndPlatform(
        @Param("customerCode") Integer customerCode,
        @Param("platform") String platform
    );
    
    /**
     * 거래처의 brandCode 조회
     * 
     * @param customerCode 거래처코드
     * @return brandCode
     */
    @Query(value = 
        "SELECT brand_code " +
        "FROM customer " +
        "WHERE customer_code = :customerCode " +
        "  AND close_dt IS NULL " +
        "LIMIT 1",
        nativeQuery = true)
    Integer findBrandCodeByCustomerCode(@Param("customerCode") Integer customerCode);
    
    /**
     * 거래처의 brandCode 조회
     * 
     * @param customerCode 거래처코드
     * @return brandCode
     */
    @Query(value = 
        "SELECT * " +
        "FROM customer " +
        "WHERE customer_code = :customerCode " +
        "  AND close_dt IS NULL " +
        "LIMIT 1",
        nativeQuery = true)
    Customer findCustomerByCustomerCode(@Param("customerCode") Integer customerCode);
}