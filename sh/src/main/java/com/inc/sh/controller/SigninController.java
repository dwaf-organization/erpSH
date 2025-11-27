package com.inc.sh.controller;

import com.inc.sh.dto.signin.reqDto.CompanyVerifyDto;
import com.inc.sh.dto.signin.reqDto.UserSigninDto;
import com.inc.sh.dto.signin.respDto.CompanyVerifyRespDto;
import com.inc.sh.dto.signin.respDto.UserSigninRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.SigninService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class SigninController {
    
    private final SigninService signinService;
    
    /**
     * 본사코드 확인
     * POST /api/v1/auth/verify-company
     */
    @PostMapping("/verify-company")
    public ResponseEntity<RespDto<CompanyVerifyRespDto>> verifyCompany(
            @RequestBody CompanyVerifyDto verifyDto) {
        
        log.info("본사코드 확인 요청 - hqAccessCode: {}", verifyDto.getHqAccessCode());
        
        RespDto<CompanyVerifyRespDto> response = signinService.verifyCompany(verifyDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사원 로그인
     * POST /api/v1/auth/signin
     */
    @PostMapping("/signin")
    public ResponseEntity<RespDto<UserSigninRespDto>> signin(
            @RequestBody UserSigninDto signinDto) {
        
        log.info("사원 로그인 요청 - hqCode: {}, userCode: {}", 
                signinDto.getHqCode(), signinDto.getUserCode());
        
        RespDto<UserSigninRespDto> response = signinService.signin(signinDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}