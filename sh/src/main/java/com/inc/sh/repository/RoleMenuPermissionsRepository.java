package com.inc.sh.repository;

import com.inc.sh.entity.RoleMenuPermissions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleMenuPermissionsRepository extends JpaRepository<RoleMenuPermissions, Integer> {
    
    /**
     * 권한별 메뉴 권한 조회
     */
    List<RoleMenuPermissions> findByRoleCode(@Param("roleCode") Integer roleCode);
    
    /**
     * 메뉴별 권한 조회
     */
    List<RoleMenuPermissions> findByMenuCode(@Param("menuCode") Integer menuCode);
    
    /**
     * 특정 권한의 특정 메뉴 권한 조회
     */
    RoleMenuPermissions findByRoleCodeAndMenuCode(@Param("roleCode") Integer roleCode, @Param("menuCode") Integer menuCode);
    
    /**
     * 권한별 메뉴 코드 리스트 조회
     */
    @Query("SELECT rmp.menuCode FROM RoleMenuPermissions rmp WHERE rmp.roleCode = :roleCode AND rmp.canView = true")
    List<Integer> findMenuCodesByRoleCode(@Param("roleCode") Integer roleCode);
    
    /**
     * 권한별 모든 메뉴 권한 삭제
     */
    @Modifying
    void deleteByRoleCode(@Param("roleCode") Integer roleCode);
    
    /**
     * 특정 권한의 특정 메뉴 권한 삭제
     */
    @Modifying
    void deleteByRoleCodeAndMenuCode(@Param("roleCode") Integer roleCode, @Param("menuCode") Integer menuCode);
}