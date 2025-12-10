package com.inc.sh.repository;

import com.inc.sh.entity.MenuInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuInfoRepository extends JpaRepository<MenuInfo, Integer> {
    
    /**
     * 모든 메뉴 조회 (순서별)
     */
    List<MenuInfo> findAllByOrderByMenuLevelAscMenuOrderAsc();
    
    /**
     * 메뉴코드로 메뉴 조회
     */
    MenuInfo findByMenuCode(@Param("menuCode") Integer menuCode);
    
    /**
     * 부모 메뉴 ID로 하위 메뉴들 조회
     */
    List<MenuInfo> findByParentIdOrderByMenuOrder(@Param("parentId") Integer parentId);
    
    /**
     * 최상위 메뉴들 조회 (parent_id가 null)
     */
    @Query("SELECT m FROM MenuInfo m WHERE m.parentId IS NULL ORDER BY m.menuOrder")
    List<MenuInfo> findTopLevelMenus();
    
    /**
     * 메뉴 레벨별 조회
     */
    List<MenuInfo> findByMenuLevelOrderByMenuOrder(@Param("menuLevel") Integer menuLevel);
    
    /**
     * 모든 메뉴 조회 (계층 순서)
     */
    @Query("SELECT m FROM MenuInfo m ORDER BY m.menuLevel, m.menuOrder, m.menuCode")
    List<MenuInfo> findAllOrderByHierarchy();
    
    /**
     * 권한별 접근 가능한 메뉴 조회 (사용자 로그인 시 사용)
     */
    @Query(value = "SELECT DISTINCT m.* FROM menu_info m " +
           "JOIN role_menu_permissions rmp ON m.menu_code = rmp.menu_code " +
           "WHERE rmp.role_code = :roleCode AND rmp.can_view = 1 " +
           "ORDER BY m.menu_level, m.menu_order, m.menu_code", nativeQuery = true)
    List<MenuInfo> findAccessibleMenusByRoleCode(@Param("roleCode") Integer roleCode);
}