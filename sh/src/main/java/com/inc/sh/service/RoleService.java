package com.inc.sh.service;

import com.inc.sh.dto.role.reqDto.RoleSearchDto;
import com.inc.sh.dto.role.reqDto.RoleSaveDto;
import com.inc.sh.dto.role.reqDto.RoleMenuSaveDto;
import com.inc.sh.dto.role.respDto.RoleRespDto;
import com.inc.sh.dto.role.respDto.MenuTreeRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    
    private final RoleRepository roleRepository;
    private final RoleMenuPermissionsRepository roleMenuPermissionsRepository;
    private final MenuInfoRepository menuInfoRepository;
    private final UserRepository userRepository;
    
    /**
     * 권한 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<RoleRespDto>> getRoleList(RoleSearchDto searchDto) {
        try {
            log.info("권한 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = roleRepository.findRolesByConditions(
                    searchDto.getRoleCode(),
                    searchDto.getRoleName()
            );
            
            List<RoleRespDto> responseList = results.stream()
                    .map(result -> RoleRespDto.builder()
                            .roleCode((Integer) result[0])
                            .roleName((String) result[1])
                            .note((String) result[2])
                            .hqCode((Integer) result[3])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("권한 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("권한 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("권한 조회 중 오류 발생", e);
            return RespDto.fail("권한 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 권한 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveRole(RoleSaveDto saveDto) {
        try {
            log.info("권한 저장 시작 - 권한코드: {}, 권한이름: {}", saveDto.getRoleCode(), saveDto.getRoleName());
            
            boolean isUpdate = saveDto.getRoleCode() != null;
            Role role;
            
            if (isUpdate) {
                // 수정
                role = roleRepository.findByRoleCode(saveDto.getRoleCode());
                if (role == null) {
                    return RespDto.fail("해당 권한을 찾을 수 없습니다.");
                }
                
                role.setRoleName(saveDto.getRoleName());
                role.setNote(saveDto.getNote());
                role.setHqCode(saveDto.getHqCode());
                role.setDescription("권한수정");
                
            } else {
                // 신규
                role = Role.builder()
                        .hqCode(saveDto.getHqCode())
                        .roleName(saveDto.getRoleName())
                        .note(saveDto.getNote())
                        .description("권한등록")
                        .build();
            }
            
            role = roleRepository.save(role);
            
            String action = isUpdate ? "수정" : "등록";
            log.info("권한 {} 완료 - 권한코드: {}", action, role.getRoleCode());
            return RespDto.success("권한이 " + action + "되었습니다.", role.getRoleCode().toString());
            
        } catch (Exception e) {
            log.error("권한 저장 중 오류 발생", e);
            return RespDto.fail("권한 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 권한 삭제
     */
    @Transactional
    public RespDto<String> deleteRole(Integer roleCode) {
        try {
            log.info("권한 삭제 시작 - 권한코드: {}", roleCode);
            
            Role role = roleRepository.findByRoleCode(roleCode);
            if (role == null) {
                return RespDto.fail("해당 권한을 찾을 수 없습니다.");
            }
            
            // 해당 권한을 사용하는 사용자 조회
            List<User> usersWithRole = userRepository.findAll().stream()
                    .filter(user -> roleCode.equals(user.getRoleCode()))
                    .collect(Collectors.toList());
            
            if (!usersWithRole.isEmpty()) {
                String userCodes = usersWithRole.stream()
                        .map(User::getUserCode)
                        .collect(Collectors.joining(", "));
                return RespDto.fail("사용중인 권한입니다. 사용자: " + userCodes);
            }
            
            // 권한 메뉴 권한 먼저 삭제
            roleMenuPermissionsRepository.deleteByRoleCode(roleCode);
            
            // 권한 삭제
            roleRepository.delete(role);
            
            log.info("권한 삭제 완료 - 권한코드: {}", roleCode);
            return RespDto.success("권한이 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("권한 삭제 중 오류 발생", e);
            return RespDto.fail("권한 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 메뉴 트리 조회 (권한별 체크 상태 포함)
     */
    @Transactional(readOnly = true)
    public RespDto<List<MenuTreeRespDto>> getMenuTreeWithPermissions(Integer roleCode) {
        try {
            log.info("메뉴 트리 조회 시작 - 권한코드: {}", roleCode);
            
            // 모든 메뉴 조회
            List<MenuInfo> allMenus = menuInfoRepository.findAllOrderByHierarchy();
            
            // 해당 권한의 메뉴 코드 리스트 조회
            Set<Integer> permittedMenuCodes = new HashSet<>();
            if (roleCode != null) {
                permittedMenuCodes.addAll(roleMenuPermissionsRepository.findMenuCodesByRoleCode(roleCode));
            }
            
            // 메뉴 트리 구조 생성
            List<MenuTreeRespDto> menuTree = buildMenuTree(allMenus, permittedMenuCodes);
            
            log.info("메뉴 트리 조회 완료 - 권한코드: {}, 트리 노드 수: {}", roleCode, menuTree.size());
            return RespDto.success("메뉴 트리 조회 성공", menuTree);
            
        } catch (Exception e) {
            log.error("메뉴 트리 조회 중 오류 발생", e);
            return RespDto.fail("메뉴 트리 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 권한별 메뉴 권한 저장
     */
    @Transactional
    public RespDto<String> saveRoleMenuPermissions(RoleMenuSaveDto saveDto) {
        try {
            log.info("권한 메뉴 저장 시작 - 권한코드: {}, 메뉴 수: {}", 
                    saveDto.getRoleCode(), saveDto.getMenuCodes() != null ? saveDto.getMenuCodes().size() : 0);
            
            // 선택된 메뉴들과 상위 메뉴들을 포함한 전체 메뉴 목록 생성
            Set<Integer> allRequiredMenus = new HashSet<>();
            if (saveDto.getMenuCodes() != null) {
                for (Integer menuCode : saveDto.getMenuCodes()) {
                    // 해당 메뉴와 모든 상위 메뉴들 추가
                    addMenuWithParents(menuCode, allRequiredMenus);
                }
            }
            
            // 기존 권한 메뉴 권한 조회
            List<Integer> existingMenuCodes = roleMenuPermissionsRepository.findMenuCodesByRoleCode(saveDto.getRoleCode());
            Set<Integer> existingMenuSet = new HashSet<>(existingMenuCodes);
            
            // 삭제할 메뉴들 (기존에 있었지만 새로운 목록에 없는 것들)
            Set<Integer> menusToDelete = new HashSet<>(existingMenuSet);
            menusToDelete.removeAll(allRequiredMenus);
            
            // 추가할 메뉴들 (새로운 목록에 있지만 기존에 없는 것들)
            Set<Integer> menusToAdd = new HashSet<>(allRequiredMenus);
            menusToAdd.removeAll(existingMenuSet);
            
            // 삭제 처리
            for (Integer menuCode : menusToDelete) {
                roleMenuPermissionsRepository.deleteByRoleCodeAndMenuCode(saveDto.getRoleCode(), menuCode);
            }
            
            // 추가 처리
            for (Integer menuCode : menusToAdd) {
                RoleMenuPermissions permission = RoleMenuPermissions.builder()
                        .roleCode(saveDto.getRoleCode())
                        .menuCode(menuCode)
                        .canView(true)
                        .description("메뉴권한설정")
                        .build();
                roleMenuPermissionsRepository.save(permission);
            }
            
            log.info("권한 메뉴 저장 완료 - 권한코드: {}, 원본 메뉴: {}개, 상위 포함 총 메뉴: {}개, 삭제: {}개, 추가: {}개", 
                    saveDto.getRoleCode(), saveDto.getMenuCodes() != null ? saveDto.getMenuCodes().size() : 0,
                    allRequiredMenus.size(), menusToDelete.size(), menusToAdd.size());
            
            return RespDto.success("권한별 메뉴가 설정되었습니다. (상위 메뉴 자동 포함)", 
                    String.format("총 메뉴: %d개 (추가: %d개, 삭제: %d개)", 
                            allRequiredMenus.size(), menusToAdd.size(), menusToDelete.size()));
            
        } catch (Exception e) {
            log.error("권한 메뉴 저장 중 오류 발생", e);
            return RespDto.fail("권한 메뉴 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 메뉴와 모든 상위 메뉴들을 Set에 추가
     */
    private void addMenuWithParents(Integer menuCode, Set<Integer> menuSet) {
        if (menuCode == null || menuSet.contains(menuCode)) {
            return;
        }
        
        MenuInfo menu = menuInfoRepository.findByMenuCode(menuCode);
        if (menu != null) {
            menuSet.add(menuCode);
            
            // 상위 메뉴가 있으면 재귀적으로 추가
            if (menu.getParentId() != null) {
                addMenuWithParents(menu.getParentId(), menuSet);
            }
        }
    }
    
    /**
     * 메뉴 트리 구조 생성
     */
    private List<MenuTreeRespDto> buildMenuTree(List<MenuInfo> allMenus, Set<Integer> permittedMenuCodes) {
        // 메뉴들을 parent_id별로 그룹화
        Map<Integer, List<MenuInfo>> menusByParent = allMenus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParentId() != null ? menu.getParentId() : 0));
        
        // 최상위 메뉴들(parent_id가 null)로 시작
        List<MenuInfo> topLevelMenus = menusByParent.getOrDefault(0, Collections.emptyList());
        
        return topLevelMenus.stream()
                .map(menu -> buildMenuNode(menu, menusByParent, permittedMenuCodes))
                .collect(Collectors.toList());
    }
    
    /**
     * 메뉴 노드 생성 (재귀)
     */
    private MenuTreeRespDto buildMenuNode(MenuInfo menu, Map<Integer, List<MenuInfo>> menusByParent, Set<Integer> permittedMenuCodes) {
        MenuTreeRespDto node = MenuTreeRespDto.builder()
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .menuLevel(menu.getMenuLevel())
                .menuOrder(menu.getMenuOrder())
                .menuPath(menu.getMenuPath())
                .isChecked(permittedMenuCodes.contains(menu.getMenuCode()))
                .build();
        
        // 하위 메뉴들 추가
        List<MenuInfo> children = menusByParent.getOrDefault(menu.getMenuCode(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<MenuTreeRespDto> childNodes = children.stream()
                    .map(child -> buildMenuNode(child, menusByParent, permittedMenuCodes))
                    .collect(Collectors.toList());
            node.setChildren(childNodes);
        }
        
        return node;
    }
    
    /**
     * 사용자별 권한에 맞는 메뉴 조회 (로그인 시 사용)
     */
    @Transactional(readOnly = true)
    public RespDto<List<MenuTreeRespDto>> getUserMenuTree(String userCode) {
        try {
            log.info("사용자별 메뉴 조회 시작 - 사용자코드: {}", userCode);
            
            // 사용자의 권한 조회
            Integer roleCode = userRepository.findRoleCodeByUserCode(userCode);
            if (roleCode == null) {
                return RespDto.fail("사용자를 찾을 수 없거나 권한이 설정되지 않았습니다.");
            }
            
            // 권한에 맞는 메뉴들 조회
            List<MenuInfo> accessibleMenus = menuInfoRepository.findAccessibleMenusByRoleCode(roleCode);
            if (accessibleMenus.isEmpty()) {
                return RespDto.fail("접근 가능한 메뉴가 없습니다.");
            }
            
            // 메뉴 트리 구조 생성 (모든 메뉴는 체크된 상태)
            Set<Integer> allAccessibleMenuCodes = accessibleMenus.stream()
                    .map(MenuInfo::getMenuCode)
                    .collect(Collectors.toSet());
            
            List<MenuTreeRespDto> menuTree = buildAccessibleMenuTree(accessibleMenus, allAccessibleMenuCodes);
            
            log.info("사용자별 메뉴 조회 완료 - 사용자코드: {}, 권한코드: {}, 메뉴 수: {}", 
                    userCode, roleCode, accessibleMenus.size());
            
            return RespDto.success("사용자별 메뉴 조회 성공", menuTree);
            
        } catch (Exception e) {
            log.error("사용자별 메뉴 조회 중 오류 발생", e);
            return RespDto.fail("사용자별 메뉴 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 접근 가능한 메뉴들로 트리 구조 생성
     */
    private List<MenuTreeRespDto> buildAccessibleMenuTree(List<MenuInfo> accessibleMenus, Set<Integer> accessibleMenuCodes) {
        // 메뉴들을 parent_id별로 그룹화
        Map<Integer, List<MenuInfo>> menusByParent = accessibleMenus.stream()
                .collect(Collectors.groupingBy(menu -> menu.getParentId() != null ? menu.getParentId() : 0));
        
        // 최상위 메뉴들(parent_id가 null) 중 접근 가능한 것들로 시작
        List<MenuInfo> accessibleTopLevelMenus = menusByParent.getOrDefault(0, Collections.emptyList());
        
        return accessibleTopLevelMenus.stream()
                .map(menu -> buildAccessibleMenuNode(menu, menusByParent, accessibleMenuCodes))
                .filter(Objects::nonNull) // null인 노드 제거
                .collect(Collectors.toList());
    }
    
    /**
     * 접근 가능한 메뉴 노드 생성 (재귀)
     */
    private MenuTreeRespDto buildAccessibleMenuNode(MenuInfo menu, Map<Integer, List<MenuInfo>> menusByParent, Set<Integer> accessibleMenuCodes) {
        MenuTreeRespDto node = MenuTreeRespDto.builder()
                .menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName())
                .parentId(menu.getParentId())
                .menuLevel(menu.getMenuLevel())
                .menuOrder(menu.getMenuOrder())
                .menuPath(menu.getMenuPath())
                .isChecked(true) // 접근 가능한 메뉴는 모두 체크된 상태
                .build();
        
        // 하위 메뉴들 중 접근 가능한 것들만 추가
        List<MenuInfo> children = menusByParent.getOrDefault(menu.getMenuCode(), Collections.emptyList());
        if (!children.isEmpty()) {
            List<MenuTreeRespDto> childNodes = children.stream()
                    .filter(child -> accessibleMenuCodes.contains(child.getMenuCode())) // 접근 가능한 메뉴만 필터링
                    .map(child -> buildAccessibleMenuNode(child, menusByParent, accessibleMenuCodes))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!childNodes.isEmpty()) {
                node.setChildren(childNodes);
            }
        }
        
        return node;
    }
}