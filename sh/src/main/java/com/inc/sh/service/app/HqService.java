package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.respDto.AppHqVerifyRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.util.HqAccessCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [앱전용] 본사 관리 서비스
 * - 본사접속코드 검증
 * - 앱에 필요한 간단한 본사 정보만 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HqService {
    
    private final HeadquarterRepository headquarterRepository;
    
    /**
     * [앱전용] 본사접속코드 검증
     * @param hqAccessCode 본사접속코드
     * @return 본사 기본 정보 (hqCode, companyName, inquiryTelNum)
     */
    @Transactional(readOnly = true)
    public RespDto<AppHqVerifyRespDto> verifyHqAccessCode(String hqAccessCode) {
        try {
            log.info("[앱] 본사접속코드 검증 시작 - hqAccessCode: '{}', 길이: {}", hqAccessCode, hqAccessCode != null ? hqAccessCode.length() : "null");
            
            // 접속코드 형식 검증
            boolean isValid = HqAccessCodeGenerator.isValidHqAccessCode(hqAccessCode);
            log.info("[앱] 형식 검증 결과: {}", isValid);
            
            if (!isValid) {
                log.warn("[앱] 잘못된 본사접속코드 형식 - hqAccessCode: '{}'", hqAccessCode);
                return RespDto.fail("잘못된 접속코드 형식입니다.");
            }
            
            // 본사 조회
            Headquarter headquarter = headquarterRepository.findByHqAccessCode(hqAccessCode);
            if (headquarter == null) {
                log.warn("[앱] 존재하지 않는 본사접속코드 - hqAccessCode: '{}'", hqAccessCode);
                return RespDto.fail("존재하지 않는 접속코드입니다.");
            }
            
            // 응답 생성 (앱에 필요한 최소 정보만)
            AppHqVerifyRespDto responseDto = AppHqVerifyRespDto.from(headquarter);
            
            log.info("[앱] 본사접속코드 검증 완료 - hqCode: {}, companyName: {}", 
                    headquarter.getHqCode(), headquarter.getCompanyName());
            
            return RespDto.success("본사 정보 확인 완료", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 본사접속코드 검증 중 오류 발생 - hqAccessCode: '{}'", hqAccessCode, e);
            return RespDto.fail("접속코드 검증 중 오류가 발생했습니다.");
        }
    }
}