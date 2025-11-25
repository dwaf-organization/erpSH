package com.inc.sh.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class HqAccessCodeGenerator {
    
    private static final String PREFIX = "SH";
    private static final String NUMBERS = "0123456789";
    private static final Random random = new Random();
    
    /**
     * 본사접속코드 생성 (SH_1111_1111 형식)
     * @return 생성된 본사접속코드
     */
    public static String generateHqAccessCode() {
        StringBuilder code = new StringBuilder();
        
        // SH_ 접두사
        code.append(PREFIX).append("_");
        
        // 첫 번째 4자리 무작위 숫자
        for (int i = 0; i < 4; i++) {
            code.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        // 구분자 _
        code.append("_");
        
        // 두 번째 4자리 무작위 숫자
        for (int i = 0; i < 4; i++) {
            code.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        }
        
        String generatedCode = code.toString();
        log.debug("본사접속코드 생성: {}", generatedCode);
        
        return generatedCode;
    }
    
    /**
     * 본사접속코드 유효성 검증
     * @param accessCode 검증할 접속코드
     * @return 유효한지 여부
     */
    public static boolean isValidHqAccessCode(String accessCode) {
        if (accessCode == null || accessCode.length() != 12) {
            return false;
        }
        
        // SH_1111_1111 형식 검증
        String pattern = "^SH_\\d{4}_\\d{4}$";
        return accessCode.matches(pattern);
    }
    
    /**
     * 테스트용 본사접속코드 생성 (여러 개)
     */
    public static void main(String[] args) {
        System.out.println("=== 본사접속코드 생성 테스트 ===");
        for (int i = 0; i < 10; i++) {
            String code = generateHqAccessCode();
            System.out.println((i + 1) + ": " + code + " (유효성: " + isValidHqAccessCode(code) + ")");
        }
    }
}