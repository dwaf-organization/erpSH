package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.contact.respDto.AppContactInfoRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.repository.HeadquarterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppContactService {
    
    private final HeadquarterRepository headquarterRepository;
    
    /**
     * [앱] 문의 정보조회 (본사정보)
     */
    @Transactional(readOnly = true)
    public RespDto<AppContactInfoRespDto> getContactInfo(Integer hqCode) {
        try {
            log.info("[앱] 문의 정보조회 시작 - hqCode: {}", hqCode);
            
            // 1. hqCode 유효성 검사
            if (hqCode == null) {
                log.warn("[앱] hqCode가 null입니다");
                return RespDto.fail("본사코드가 입력되지 않았습니다");
            }
            
            // 2. 본사 정보 조회
            Headquarter headquarter = headquarterRepository.findByHqCode(hqCode);
            if (headquarter == null) {
                log.warn("[앱] 존재하지 않는 본사 - hqCode: {}", hqCode);
                return RespDto.fail("존재하지 않는 본사입니다");
            }
            
            // 3. 응답 DTO 생성 (null 값은 '-'로 처리)
            AppContactInfoRespDto responseDto = AppContactInfoRespDto.builder()
                    .companyName(nullToHyphen(headquarter.getCompanyName()))
                    .ceoName(nullToHyphen(headquarter.getCeoName()))
                    .bizType(nullToHyphen(headquarter.getBizType()))
                    .bizItem(nullToHyphen(headquarter.getBizItem()))
                    .homepage(nullToHyphen(headquarter.getHomepage()))
                    .faxNum(nullToHyphen(headquarter.getFaxNum()))
                    .inquiryTelNum(nullToHyphen(headquarter.getInquiryTelNum()))
                    .build();
            
            log.info("[앱] 문의 정보조회 완료 - hqCode: {}, companyName: {}", 
                    hqCode, headquarter.getCompanyName());
            
            return RespDto.success("문의 정보 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 문의 정보조회 실패 - hqCode: {}", hqCode, e);
            return RespDto.fail("문의 정보 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * null 값을 '-'로 변환
     */
    private String nullToHyphen(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value.trim();
    }
}