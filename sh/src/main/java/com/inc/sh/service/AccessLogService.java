package com.inc.sh.service;

import com.inc.sh.entity.UserAccessLog;
import com.inc.sh.repository.UserAccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessLogService {
    
    private final UserAccessLogRepository accessLogRepository;
    
    /**
     * 로그인 성공 로그 기록
     */
    @Transactional
    public void logSuccessAccess(String userType, String userCode, Integer hqCode, HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            
            UserAccessLog accessLog = UserAccessLog.builder()
                    .userType(userType)
                    .userCode(userCode)
                    .hqCode(hqCode)
                    .loginStatus("SUCCESS")
                    .ipAddress(clientIp)
                    .build();
            
            accessLogRepository.save(accessLog);
            
            log.info("로그인 성공 로그 기록 완료 - userType: {}, userCode: {}, ip: {}", 
                    userType, userCode, clientIp);
            
        } catch (Exception e) {
            log.error("로그인 성공 로그 기록 중 오류 발생 - userType: {}, userCode: {}", 
                    userType, userCode, e);
        }
    }
    
    /**
     * 로그인 실패 로그 기록
     */
    @Transactional
    public void logFailureAccess(String userType, String userCode, Integer hqCode, 
                                String failureReason, HttpServletRequest request) {
        try {
            String clientIp = getClientIp(request);
            
            UserAccessLog accessLog = UserAccessLog.builder()
                    .userType(userType)
                    .userCode(userCode)
                    .hqCode(hqCode)
                    .loginStatus("FAILURE")
                    .ipAddress(clientIp)
                    .failureReason(failureReason)
                    .build();
            
            accessLogRepository.save(accessLog);
            
            log.info("로그인 실패 로그 기록 완료 - userType: {}, userCode: {}, reason: {}, ip: {}", 
                    userType, userCode, failureReason, clientIp);
            
        } catch (Exception e) {
            log.error("로그인 실패 로그 기록 중 오류 발생 - userType: {}, userCode: {}", 
                    userType, userCode, e);
        }
    }
    
    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 여러 IP가 있는 경우 첫 번째 IP 사용
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}