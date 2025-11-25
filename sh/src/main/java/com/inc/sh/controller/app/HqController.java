package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.app.respDto.AppMainRespDto;
import com.inc.sh.dto.customerUser.reqDto.AppLoginReqDto;
import com.inc.sh.dto.customerUser.respDto.AppLoginRespDto;
import com.inc.sh.dto.headquarter.respDto.AppHqVerifyRespDto;
import com.inc.sh.service.app.AppCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * [앱전용] 본사 및 기본 관리 컨트롤러
 * - 본사접속코드 검증
 * - 로그인
 * - 메인페이지 정보 조회
 */
@RestController
@RequestMapping("/api/v1/app")
@RequiredArgsConstructor
@Slf4j
public class HqController {
    
    private final AppCustomerService appCustomerService;
    
    /**
     * [앱전용] 본사접속코드 검증
     * POST /api/v1/app/verify-hq
     */
    @PostMapping("/verify-hq")
    public ResponseEntity<RespDto<AppHqVerifyRespDto>> verifyHqAccessCode(
            @RequestParam("hq_access_code") String hqAccessCode) {
        
        log.info("[앱] 본사접속코드 검증 요청 - hqAccessCode: {}", hqAccessCode);
        
        RespDto<AppHqVerifyRespDto> response = appCustomerService.verifyHqAccessCode(hqAccessCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 고객 사용자 로그인
     * POST /api/v1/app/login
     */
    @PostMapping("/login")
    public ResponseEntity<RespDto<AppLoginRespDto>> login(
            @Valid @RequestBody AppLoginReqDto request) {
        
        log.info("[앱] 로그인 요청 - customerUserId: {}", request.getCustomerUserId());
        
        RespDto<AppLoginRespDto> response = appCustomerService.login(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 메인페이지 정보 조회
     * GET /api/v1/app/main
     */
    @GetMapping("/main")
    public ResponseEntity<RespDto<AppMainRespDto>> getMainInfo(
            @RequestParam("customer_code") Integer customerCode) {
        
        log.info("[앱] 메인페이지 정보 조회 요청 - customerCode: {}", customerCode);
        
        RespDto<AppMainRespDto> response = appCustomerService.getMainInfo(customerCode);
        
        return ResponseEntity.ok(response);
    }
}