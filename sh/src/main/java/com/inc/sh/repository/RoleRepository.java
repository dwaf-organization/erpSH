package com.inc.sh.repository;

import com.inc.sh.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * 권한코드로 조회
     */
    Role findByRoleCode(Integer roleCode);
    
    /**
     * 권한코드 존재 여부 확인
     */
    boolean existsByRoleCode(Integer roleCode);
    
    /**
     * 권한명으로 조회
     */
    Role findByRoleName(String roleName);
    
    /**
     * 권한명 존재 여부 확인
     */
    boolean existsByRoleName(String roleName);
    
    /**
     * 본사별 권한 조회
     */
    List<Role> findByHqCode(Integer hqCode);
    
    /**
     * 본사별 권한명으로 조회
     */
    Role findByHqCodeAndRoleName(Integer hqCode, String roleName);
    
    /**
     * 본사별 권한명 존재 여부 확인
     */
    boolean existsByHqCodeAndRoleName(Integer hqCode, String roleName);
    
    /**
     * 권한명으로 부분 검색
     */
    @Query("SELECT r FROM Role r WHERE r.roleName LIKE %:roleName% ORDER BY r.roleCode ASC")
    List<Role> findByRoleNameContaining(@Param("roleName") String roleName);
    
    /**
     * 본사별 권한명으로 부분 검색
     */
    @Query("SELECT r FROM Role r WHERE r.hqCode = :hqCode AND r.roleName LIKE %:roleName% ORDER BY r.roleCode ASC")
    List<Role> findByHqCodeAndRoleNameContaining(@Param("hqCode") Integer hqCode, @Param("roleName") String roleName);
    
    /**
     * 모든 권한 조회 (권한코드 순)
     */
    @Query("SELECT r FROM Role r ORDER BY r.roleCode ASC")
    List<Role> findAllOrderByRoleCode();
    
    /**
     * 본사별 권한 조회 (권한코드 순)
     */
    @Query("SELECT r FROM Role r WHERE r.hqCode = :hqCode ORDER BY r.roleCode ASC")
    List<Role> findByHqCodeOrderByRoleCode(@Param("hqCode") Integer hqCode);

    /**
     * 권한 검색 조회 (hqCode 조건 추가)
     */
    @Query(value = "SELECT " +
           "r.role_code, " +
           "r.role_name, " +
           "r.note, " +
           "r.hq_code " +
           "FROM role r " +
           "WHERE r.hq_code = :hqCode " +
           "AND (:roleCode IS NULL OR CAST(r.role_code AS CHAR) LIKE CONCAT('%', :roleCode, '%')) " +
           "AND (:roleName IS NULL OR r.role_name LIKE CONCAT('%', :roleName, '%')) " +
           "ORDER BY r.role_code", nativeQuery = true)
    List<Object[]> findRolesByConditionsAndHqCode(
        @Param("hqCode") Integer hqCode,
        @Param("roleCode") String roleCode,
        @Param("roleName") String roleName
    );
}