package com.inc.sh.controller;

import com.inc.sh.dto.role.reqDto.RoleSearchDto;
import com.inc.sh.dto.role.reqDto.RoleSaveReqDto;
import com.inc.sh.dto.role.reqDto.RoleDeleteReqDto;
import com.inc.sh.dto.role.reqDto.RoleMenuSaveDto;
import com.inc.sh.dto.role.respDto.RoleRespDto;
import com.inc.sh.dto.role.respDto.RoleBatchResult;
import com.inc.sh.dto.role.respDto.MenuTreeRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/role")
@RequiredArgsConstructor
@Slf4j
public class RoleController {

    private final RoleService roleService;

    /**
     * 권한 조회 (hqCode 필수)
     * GET /api/v1/erp/role/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<RoleRespDto>>> getRoleList(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "roleCode", required = false) String roleCode,
            @RequestParam(value = "roleName", required = false) String roleName) {
        
        log.info("권한 조회 요청 - hqCode: {}, 권한코드: {}, 권한이름: {}", hqCode, roleCode, roleName);
        
        RoleSearchDto searchDto = RoleSearchDto.builder()
                .hqCode(hqCode)
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
     * 권한 다중 저장 (신규/수정)
     * POST /api/v1/erp/role/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<RoleBatchResult>> saveRoles(@RequestBody RoleSaveReqDto request) {
        
        log.info("권한 다중 저장 요청 - 총 {}건", 
                request.getRoles() != null ? request.getRoles().size() : 0);
        
        // 요청 데이터 검증
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 권한 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (int i = 0; i < request.getRoles().size(); i++) {
            RoleSaveReqDto.RoleSaveItemDto item = request.getRoles().get(i);
            
            if (item.getRoleName() == null || item.getRoleName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 권한이름은 필수입니다.", i + 1)));
            }
            if (item.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 본사코드는 필수입니다.", i + 1)));
            }
        }
        
        RespDto<RoleBatchResult> response = roleService.saveRoles(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 권한 다중 삭제
     * DELETE /api/v1/erp/role/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<RoleBatchResult>> deleteRoles(@RequestBody RoleDeleteReqDto request) {
        
        log.info("권한 다중 삭제 요청 - 총 {}건", 
                request.getRoleCodes() != null ? request.getRoleCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getRoleCodes() == null || request.getRoleCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 권한코드가 없습니다."));
        }
        
        // 중복 제거 및 null 제거
        List<Integer> validRoleCodes = request.getRoleCodes().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (validRoleCodes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 권한코드가 없습니다."));
        }
        
        if (validRoleCodes.size() != request.getRoleCodes().size()) {
            log.info("중복/null 권한코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getRoleCodes().size(), validRoleCodes.size());
            request.setRoleCodes(validRoleCodes);
        }
        
        RespDto<RoleBatchResult> response = roleService.deleteRoles(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 메뉴 트리 조회 (권한별 체크 상태 포함) - 기존 유지
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
     * 권한별 메뉴 권한 저장 - 기존 유지
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
     * 사용자별 권한에 맞는 메뉴 조회 (로그인 시 사용) - 기존 유지
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