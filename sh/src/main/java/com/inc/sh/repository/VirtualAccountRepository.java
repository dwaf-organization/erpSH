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
     * 가상계좌코드로 가상계좌 조회
     */
    VirtualAccount findByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 가상계좌코드 존재 여부 확인
     */
    boolean existsByVirtualAccountCode(Integer virtualAccountCode);
    
    /**
     * 복합 조건으로 가상계좌 조회
     * @param linkedCustomerCode 연결거래처코드 (null 가능)
     * @param virtualAccountStatus 가상계좌상태 ("전체", "사용", "미사용")
     * @param closeDtYn 해지여부 ("전체", "Y", "N")
     * @return 조회된 가상계좌 목록
     */
    @Query("SELECT va FROM VirtualAccount va WHERE " +
           "(:linkedCustomerCode IS NULL OR va.linkedCustomerCode = :linkedCustomerCode) AND " +
           "(:virtualAccountStatus = '전체' OR " +
           "  (CASE WHEN :virtualAccountStatus = '사용' THEN va.virtualAccountStatus = '사용중' " +
           "        WHEN :virtualAccountStatus = '미사용' THEN va.virtualAccountStatus = '미사용' END)) AND " +
           "(:closeDtYn = '전체' OR " +
           "  (CASE WHEN :closeDtYn = 'Y' THEN va.closeDt IS NOT NULL " +
           "        WHEN :closeDtYn = 'N' THEN va.closeDt IS NULL END)) " +
           "ORDER BY va.virtualAccountCode DESC")
    List<VirtualAccount> findBySearchConditions(
        @Param("linkedCustomerCode") Integer linkedCustomerCode,
        @Param("virtualAccountStatus") String virtualAccountStatus,
        @Param("closeDtYn") String closeDtYn
    );
    
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
     * 팝업용 가상계좌 검색
     * @param virtualAccountCode 가상계좌코드 (부분일치, null 가능)
     * @param virtualAccount 가상계좌번호 (부분일치, null 가능)
     * @param accountStatus 계좌상태 (완전일치, null 가능)
     * @return 조회된 가상계좌 목록
     */
    @Query(value = "SELECT * FROM virtual_account va WHERE " +
           "(:virtualAccountCode IS NULL OR CAST(va.virtual_account_code AS CHAR) LIKE CONCAT('%', :virtualAccountCode, '%')) AND " +
           "(:virtualAccount IS NULL OR va.virtual_account_num LIKE CONCAT('%', :virtualAccount, '%')) AND " +
           "(:accountStatus IS NULL OR va.virtual_account_status = :accountStatus) " +
           "ORDER BY va.virtual_account_code ASC", nativeQuery = true)
    List<VirtualAccount> findByPopupSearchConditions(
        @Param("virtualAccountCode") String virtualAccountCode,
        @Param("virtualAccount") String virtualAccount,
        @Param("accountStatus") String accountStatus
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