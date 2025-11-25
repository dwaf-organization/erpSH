package com.inc.sh.controller;

import com.inc.sh.dto.role.reqDto.RoleSearchDto;
import com.inc.sh.dto.role.reqDto.RoleSaveDto;
import com.inc.sh.dto.role.reqDto.RoleMenuSaveDto;
import com.inc.sh.dto.role.respDto.RoleRespDto;
import com.inc.sh.dto.role.respDto.MenuTreeRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/role")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    /**
     * 권한 조회
     * GET /api/v1/erp/role/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<RoleRespDto>>> getRoleList(
            @RequestParam(value = "roleCode", required = false) String roleCode,
            @RequestParam(value = "roleName", required = false) String roleName) {
        
        log.info("권한 조회 요청 - 권한코드: {}, 권한이름: {}", roleCode, roleName);
        
        RoleSearchDto searchDto = RoleSearchDto.builder()
                .roleCode(roleCode)
                .roleName(roleName)
                .build();
        
        RespDto<List<RoleRespDto>> response = roleService.getRoleList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 권한 저장 (신규/수정)
     * POST /api/v1/erp/role/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveRole(@RequestBody RoleSaveDto saveDto) {
        
        log.info("권한 저장 요청 - 권한코드: {}, 권한이름: {}", saveDto.getRoleCode(), saveDto.getRoleName());
        
        RespDto<String> response = roleService.saveRole(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 권한 삭제
     * DELETE /api/v1/erp/role/{roleCode}
     */
    @DeleteMapping("/{roleCode}")
    public ResponseEntity<RespDto<String>> deleteRole(@PathVariable Integer roleCode) {
        
        log.info("권한 삭제 요청 - 권한코드: {}", roleCode);
        
        RespDto<String> response = roleService.deleteRole(roleCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 메뉴 트리 조회 (권한별 체크 상태 포함)
     * GET /api/v1/erp/role/menu-permissions
     */
    @GetMapping("/menu-permissions")
    public ResponseEntity<RespDto<List<MenuTreeRespDto>>> getMenuTreeWithPermissions(
            @RequestParam(value = "roleCode", required = false) Integer roleCode) {
        
        log.info("메뉴 트리 조회 요청 - 권한코드: {}", roleCode);
        
        RespDto<List<MenuTreeRespDto>> response = roleService.getMenuTreeWithPermissions(roleCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 권한별 메뉴 권한 저장
     * POST /api/v1/erp/role/menu-permissions/save
     */
    @PostMapping("/menu-permissions/save")
    public ResponseEntity<RespDto<String>> saveRoleMenuPermissions(@RequestBody RoleMenuSaveDto saveDto) {
        
        log.info("권한 메뉴 저장 요청 - 권한코드: {}, 메뉴 수: {}", 
                saveDto.getRoleCode(), saveDto.getMenuCodes() != null ? saveDto.getMenuCodes().size() : 0);
        
        RespDto<String> response = roleService.saveRoleMenuPermissions(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자별 권한에 맞는 메뉴 조회 (로그인 시 사용)
     * GET /api/v1/erp/role/user-menus
     */
    @GetMapping("/user-menus")
    public ResponseEntity<RespDto<List<MenuTreeRespDto>>> getUserMenuTree(
            @RequestParam("userCode") String userCode) {
        
        log.info("사용자별 메뉴 조회 요청 - 사용자코드: {}", userCode);
        
        RespDto<List<MenuTreeRespDto>> response = roleService.getUserMenuTree(userCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}