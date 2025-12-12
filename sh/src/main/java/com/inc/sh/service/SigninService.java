package com.inc.sh.service;

import com.inc.sh.dto.signin.reqDto.CompanyVerifyDto;
import com.inc.sh.dto.signin.reqDto.UserSigninDto;
import com.inc.sh.dto.signin.respDto.CompanyVerifyRespDto;
import com.inc.sh.dto.signin.respDto.UserSigninRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.entity.User;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SigninService {
    
    private final HeadquarterRepository headquarterRepository;
    private final UserRepository userRepository;
    
    private final AccessLogService accessLogService;
    
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 본사코드 확인 (본사접속코드로)
     */
    @Transactional(readOnly = true)
    public RespDto<CompanyVerifyRespDto> verifyCompany(CompanyVerifyDto verifyDto) {
        try {
            log.info("본사코드 확인 시작 - hqAccessCode: {}", verifyDto.getHqAccessCode());
            
            // 본사접속코드로 본사 조회
            Headquarter headquarter = headquarterRepository.findByHqAccessCode(verifyDto.getHqAccessCode());
            if (headquarter == null) {
                return RespDto.fail("존재하지 않는 본사접속코드입니다.");
            }
            
            // 응답 데이터 구성
            CompanyVerifyRespDto responseData = CompanyVerifyRespDto.builder()
                    .hqCode(headquarter.getHqCode())
                    .hqAccessCode(headquarter.getHqAccessCode())
                    .hqName(headquarter.getCompanyName())
                    .build();
            
            log.info("본사코드 확인 성공 - hqCode: {}, hqName: {}", 
                    headquarter.getHqCode(), headquarter.getCompanyName());
            
            return RespDto.success("본사코드 확인 완료", responseData);
            
        } catch (Exception e) {
            log.error("본사코드 확인 중 오류 발생", e);
            return RespDto.fail("본사코드 확인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사원 로그인
     */
    @Transactional(readOnly = true)
    public RespDto<UserSigninRespDto> signin(UserSigninDto signinDto) {
        try {
            log.info("사원 로그인 시작 - hqCode: {}, userCode: {}", 
                    signinDto.getHqCode(), signinDto.getUserCode());
            
            // 1. 본사코드와 사원코드로 사원 조회
            User user = userRepository.findByHqCodeAndUserCode(
                    signinDto.getHqCode(), signinDto.getUserCode());
            if (user == null) {
                return RespDto.fail("존재하지 않는 사원정보입니다.");
            }
            
            // 2. 비밀번호 확인
            if (!passwordEncoder.matches(signinDto.getUserPw(), user.getUserPw())) {
                log.warn("비밀번호 불일치 - hqCode: {}, userCode: {}", 
                        signinDto.getHqCode(), signinDto.getUserCode());
                return RespDto.fail("비밀번호가 올바르지 않습니다.");
            }
            
            
            // 4. 응답 데이터 구성
            UserSigninRespDto responseData = UserSigninRespDto.builder()
                    .userCode(user.getUserCode())
                    .roleCode(user.getRoleCode())
                    .userName(user.getUserName())
                    .build();
            
            log.info("사원 로그인 성공 - userCode: {}, userName: {}, roleCode: {}", 
                    user.getUserCode(), user.getUserName(), user.getRoleCode());
            
            return RespDto.success("로그인 성공", responseData);
            
        } catch (Exception e) {
            log.error("사원 로그인 중 오류 발생", e);
            return RespDto.fail("로그인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사원 로그인 (로그 기능 추가)
     */
    @Transactional(readOnly = true)
    public RespDto<UserSigninRespDto> signin(UserSigninDto signinDto, HttpServletRequest request) {
        try {
            log.info("사원 로그인 시작 - hqCode: {}, userCode: {}", 
                    signinDto.getHqCode(), signinDto.getUserCode());
            
            // 1. 본사코드와 사원코드로 사원 조회
            User user = userRepository.findByHqCodeAndUserCode(
                    signinDto.getHqCode(), signinDto.getUserCode());
            if (user == null) {
                // 실패 로그 기록
                accessLogService.logFailureAccess("ERP", signinDto.getUserCode(), 
                        signinDto.getHqCode(), "사원정보 없음", request);
                return RespDto.fail("존재하지 않는 사원정보입니다.");
            }
            
            // 2. 비밀번호 확인
            if (!passwordEncoder.matches(signinDto.getUserPw(), user.getUserPw())) {
                log.warn("비밀번호 불일치 - hqCode: {}, userCode: {}", 
                        signinDto.getHqCode(), signinDto.getUserCode());
                
                // 실패 로그 기록
                accessLogService.logFailureAccess("ERP", signinDto.getUserCode(), 
                        signinDto.getHqCode(), "비밀번호 불일치", request);
                return RespDto.fail("비밀번호가 올바르지 않습니다.");
            }
            
            // 3. 로그인 성공 로그 기록
            accessLogService.logSuccessAccess("ERP", user.getUserCode(), 
                    user.getHqCode(), request);
            
            // 4. 응답 데이터 구성
            UserSigninRespDto responseData = UserSigninRespDto.builder()
                    .userCode(user.getUserCode())
                    .roleCode(user.getRoleCode())
                    .userName(user.getUserName())
                    .build();
            
            log.info("사원 로그인 성공 - userCode: {}, userName: {}, roleCode: {}", 
                    user.getUserCode(), user.getUserName(), user.getRoleCode());
            
            return RespDto.success("로그인 성공", responseData);
            
        } catch (Exception e) {
            log.error("사원 로그인 중 오류 발생", e);
            return RespDto.fail("로그인 중 오류가 발생했습니다.");
        }
    }
    
}