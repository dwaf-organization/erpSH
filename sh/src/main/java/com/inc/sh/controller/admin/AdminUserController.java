package com.inc.sh.controller.admin;

import com.inc.sh.dto.user.reqDto.AdminUserReqDto;
import com.inc.sh.dto.user.respDto.AdminUserDetailRespDto;
import com.inc.sh.dto.user.respDto.AdminUserListRespDto;
import com.inc.sh.dto.user.respDto.AdminUserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.admin.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * 관리자 사용자 목록 조회
     * GET /api/v1/admin/users/list?hqCode={hqCode}
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<AdminUserListRespDto>>> getUserList(
            @RequestParam(value = "hqCode", required = false) Integer hqCode) {
        
        String searchType = (hqCode == null) ? "전체 조회" : "본사별 조회";
        log.info("관리자 사용자 목록 조회 요청 - {}, hqCode: {}", searchType, hqCode);
        
        RespDto<List<AdminUserListRespDto>> response = adminUserService.getUserList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 관리자 사용자 상세 조회
     * GET /api/v1/admin/users/one?userCode={userCode}
     */
    @GetMapping("/one")
    public ResponseEntity<RespDto<AdminUserDetailRespDto>> getUserDetail(
            @RequestParam("userCode") String userCode) {
        
        log.info("관리자 사용자 상세 조회 요청 - userCode: {}", userCode);
        
        RespDto<AdminUserDetailRespDto> response = adminUserService.getUserDetail(userCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 관리자 사용자 저장 (생성/수정 통합)
     * POST /api/v1/admin/users/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<AdminUserRespDto>> saveUser(
            @Valid @RequestBody AdminUserReqDto request) {
        
        String action = request.getUserCode() == null ? "생성" : "수정";
        log.info("관리자 사용자 {} 요청 - userCode: {}, userName: {}", action, request.getUserCode(), request.getUserName());
        
        RespDto<AdminUserRespDto> response = adminUserService.saveUser(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 관리자 사용자 삭제
     * DELETE /api/v1/admin/users/delete?userCode={userCode}
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<String>> deleteUser(
            @RequestParam("userCode") String userCode) {
        
        log.info("관리자 사용자 삭제 요청 - userCode: {}", userCode);
        
        RespDto<String> response = adminUserService.deleteUser(userCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}