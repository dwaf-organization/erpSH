package com.inc.sh.repository;

import com.inc.sh.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Integer> {
    /**
     * 거래처코드와 상태로 가상계좌들 조회 (선택적 사용)
     */
    List<VirtualAccount> findByLinkedCustomerCodeAndVirtualAccountStatus(
            Integer linkedCustomerCode, String virtualAccountStatus);
    
    /**
     * 거래처별 가상계좌 개수 조회 (통계용)
     */
    @Query(value = "SELECT COUNT(*) FROM virtual_account " +
           "WHERE linked_customer_code = :customerCode", nativeQuery = true)
    Long countByLinkedCustomerCode(@Param("customerCode") Integer customerCode);
    
    /**
     * 가상계좌코드로 가상계좌 조회
     */
    VirtualAccount findByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 가상계좌코드 존재 여부 확인
     */
    boolean existsByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 복합 조건으로 가상계좌 조회 (customer_name 조인 포함)
     * @param hqCode 본사코드 (필수)
     * @param linkedCustomerCode 연결거래처코드 (null 가능)
     * @param virtualAccountStatus 가상계좌상태 ("전체", "사용", "미사용")
     * @param closeDtYn 해지여부 ("전체", "Y", "N")
     * @return VirtualAccount, customerName을 포함한 Object[] 배열
     */
    @Query(value = "SELECT " +
           "va.virtual_account_code, " +        // 0
           "va.hq_code, " +                     // 1
           "va.virtual_account_num, " +         // 2
           "va.virtual_account_status, " +      // 3
           "va.bank_name, " +                   // 4
           "va.linked_customer_code, " +        // 5
           "va.open_dt, " +                     // 6
           "va.close_dt, " +                    // 7
           "va.note, " +                        // 8
           "c.customer_name " +                 // 9 - customer_name 추가
           "FROM virtual_account va " +
           "LEFT JOIN customer c ON va.linked_customer_code = c.customer_code AND c.hq_code = va.hq_code " +
           "WHERE va.hq_code = :hqCode " +
           "AND (:linkedCustomerCode IS NULL OR va.linked_customer_code = :linkedCustomerCode) " +
           "AND (:virtualAccountStatus = '' OR " +
           "     (CASE WHEN :virtualAccountStatus = '사용' THEN va.virtual_account_status = '사용' " +
           "           WHEN :virtualAccountStatus = '미사용' THEN va.virtual_account_status = '미사용' END)) " +
           "AND (:closeDtYn = '' OR " +
           "     (CASE WHEN :closeDtYn = 'Y' THEN va.close_dt IS NOT NULL " +
           "           WHEN :closeDtYn = 'N' THEN va.close_dt IS NULL END)) " +
           "ORDER BY va.virtual_account_code DESC", nativeQuery = true)
    List<Object[]> findBySearchConditionsWithJoin(
        @Param("hqCode") Integer hqCode,
        @Param("linkedCustomerCode") Integer linkedCustomerCode,
        @Param("virtualAccountStatus") String virtualAccountStatus,
        @Param("closeDtYn") String closeDtYn
    );

    /**
     * 가상계좌번호 중복 체크 (수정시 자신 제외)
     */
    boolean existsByVirtualAccountNumAndVirtualAccountCodeNot(String virtualAccountNum, Integer virtualAccountCode);
    
    /**
     * 연결된 거래처가 있는 가상계좌 조회
     */
    @Query("SELECT va FROM VirtualAccount va WHERE va.linkedCustomerCode IS NOT NULL")
    List<VirtualAccount> findByLinkedCustomerCodeIsNotNull();
    
    /**
     * 특정 거래처에 연결된 가상계좌 조회 (복수)
     */
    List<VirtualAccount> findByLinkedCustomerCode(Integer linkedCustomerCode);
    
    /**
     * 특정 거래처에 연결된 가상계좌 조회 (단건) - 중복 체크용
     */
    VirtualAccount findFirstByLinkedCustomerCode(Integer linkedCustomerCode);
    
    /**
     * 가상계좌 번호로 조회
     */
    VirtualAccount findByVirtualAccountNum(String virtualAccountNum);
    
    /**
     * 가상계좌 번호 중복 확인
     */
    boolean existsByVirtualAccountNum(String virtualAccountNum);
    
    /**
     * 가상계좌 팝업 검색 (customer_name 조인 포함 - 새로 추가)
     * @param hqCode 본사코드 (필수)
     * @param virtualAccountNum 가상계좌번호 (부분일치, null 가능)
     * @param virtualAccountStatus 가상계좌상태 (완전일치, null 가능)
     * @return VirtualAccount, customerName을 포함한 Object[] 배열
     */
    @Query(value = "SELECT " +
           "va.virtual_account_code, " +        // 0
           "va.hq_code, " +                     // 1
           "va.virtual_account_num, " +         // 2
           "va.virtual_account_status, " +      // 3
           "va.bank_name, " +                   // 4
           "va.linked_customer_code, " +        // 5
           "va.open_dt, " +                     // 6
           "va.close_dt, " +                    // 7
           "va.note, " +                        // 8
           "c.customer_name " +                 // 9
           "FROM virtual_account va " +
           "LEFT JOIN customer c ON va.linked_customer_code = c.customer_code AND c.hq_code = va.hq_code " +
           "WHERE va.hq_code = :hqCode " +
           "AND (:virtualAccountNum IS NULL OR va.virtual_account_num LIKE CONCAT('%', :virtualAccountNum, '%')) " +
           "AND (:virtualAccountStatus IS NULL OR va.virtual_account_status = :virtualAccountStatus) " +
           "ORDER BY va.virtual_account_code ASC", nativeQuery = true)
    List<Object[]> findByPopupSearchConditionsWithJoin(
        @Param("hqCode") Integer hqCode,
        @Param("virtualAccountNum") String virtualAccountNum,
        @Param("virtualAccountStatus") String virtualAccountStatus
    );
    
    /**
     * [앱전용] 거래처별 가상계좌 정보 조회
     */
    @Query(value = "SELECT va.virtual_account_num, va.bank_name, va.virtual_account_status " +
           "FROM virtual_account va " +
           "WHERE va.linked_customer_code = :customerCode " +
           "LIMIT 1", nativeQuery = true)
    List<Object[]> findVirtualAccountByCustomerCode(@Param("customerCode") Integer customerCode);
}