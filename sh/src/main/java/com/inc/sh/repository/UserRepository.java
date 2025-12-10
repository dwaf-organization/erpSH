package com.inc.sh.repository;

import com.inc.sh.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * 사번으로 사용자 조회
     */
    User findByUserCode(@Param("userCode") String userCode);
    
    /**
     * 본사코드와 사원코드로 사원 조회
     */
    User findByHqCodeAndUserCode(@Param("hqCode") Integer hqCode, @Param("userCode") String userCode);
    
    /**
     * 사번 존재 여부 확인
     */
    boolean existsByUserCode(@Param("userCode") String userCode);
    
    /**
     * 특정 prefix로 시작하는 마지막 사용자 코드 조회 (YYMM+hqCode+001 형태용)
     */
    @Query(value = "SELECT u.user_code " +
           "FROM user u " +
           "WHERE u.user_code LIKE CONCAT(:prefix, '%') " +
           "ORDER BY u.user_code DESC " +
           "LIMIT 1", nativeQuery = true)
    String findLastUserCodeByPrefix(@Param("prefix") String prefix);
    
    /**
     * 사용자 검색 조회 (권한명 포함)
     */
    @Query(value = "SELECT " +
           "u.user_code, " +
           "u.user_name, " +
           "u.phone1, " +
           "u.phone2, " +
           "u.email, " +
           "u.resignation_dt, " +
           "u.role_code, " +
           "r.role_name, " +
           "u.user_pw " +
           "FROM user u " +
           "LEFT JOIN role r ON u.role_code = r.role_code " +
           "WHERE (:userCode IS NULL OR u.user_code LIKE CONCAT('%', :userCode, '%')) " +
           "AND (:userName IS NULL OR u.user_name LIKE CONCAT('%', :userName, '%')) " +
           "ORDER BY u.user_code", nativeQuery = true)
    List<Object[]> findUsersWithRoleByConditions(
        @Param("userCode") String userCode,
        @Param("userName") String userName
    );
    
    /**
     * 년월별 마지막 사번 조회 (시퀀스 생성용)
     */
    @Query(value = "SELECT user_code FROM user " +
           "WHERE user_code LIKE CONCAT(:yearMonth, '%') " +
           "ORDER BY user_code DESC LIMIT 1", nativeQuery = true)
    String findLastUserCodeByYearMonth(@Param("yearMonth") String yearMonth);

    /**
     * 사용자 권한으로 메뉴 조회 (로그인 사용자용)
     */
    @Query(value = "SELECT u.role_code FROM user u WHERE u.user_code = :userCode", nativeQuery = true)
    Integer findRoleCodeByUserCode(@Param("userCode") String userCode);
}