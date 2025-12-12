package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.reqDto.SiteInfoReqDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.dto.headquarter.respDto.SiteInfoRespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import com.inc.sh.util.HqAccessCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeadquarterService {

    private final HeadquarterRepository headquarterRepository;

    /**
     * 본사 정보 조회
     * @param hqCode 본사코드
     * @return 본사 정보
     */
    @Transactional(readOnly = true)
    public RespDto<SiteInfoRespDto> getSiteInfo(Integer hqCode) {
        try {
            log.info("본사 정보 조회 시작 - hqCode: {}", hqCode);
            
            Headquarter headquarter = headquarterRepository.findByHqCode(hqCode);
            if (headquarter == null) {
                log.warn("본사 정보를 찾을 수 없습니다 - hqCode: {}", hqCode);
                return RespDto.fail("본사 정보를 찾을 수 없습니다.");
            }
            
            SiteInfoRespDto responseDto = SiteInfoRespDto.fromEntity(headquarter);
            
            log.info("본사 정보 조회 완료 - hqCode: {}, companyName: {}", hqCode, headquarter.getCompanyName());
            return RespDto.success("본사 정보 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("본사 정보 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("본사 정보 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 본사 정보 수정
     * @param request 본사 정보 수정 요청
     * @return 수정된 본사 정보
     */
    @Transactional
    public RespDto<SiteInfoRespDto> updateSiteInfo(SiteInfoReqDto request) {
        try {
            log.info("본사 정보 수정 시작 - hqCode: {}, companyName: {}", 
                    request.getHqCode(), request.getCompanyName());
            
            // hqCode로 본사 정보 조회
            Headquarter headquarter = headquarterRepository.findByHqCode(request.getHqCode());
            
            if (headquarter == null) {
                log.warn("수정할 본사 정보를 찾을 수 없습니다 - hqCode: {}", request.getHqCode());
                return RespDto.fail("수정할 본사 정보를 찾을 수 없습니다.");
            }
            
            // 본사 정보 업데이트
            request.updateEntity(headquarter);
            
            // 저장
            Headquarter savedHeadquarter = headquarterRepository.save(headquarter);
            
            // 응답 생성
            SiteInfoRespDto responseDto = SiteInfoRespDto.fromEntity(savedHeadquarter);
            
            log.info("본사 정보 수정 완료 - hqCode: {}, companyName: {}", 
                    savedHeadquarter.getHqCode(), savedHeadquarter.getCompanyName());
            
            return RespDto.success("본사 정보가 성공적으로 수정되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("본사 정보 수정 중 오류 발생 - hqCode: {}", request.getHqCode(), e);
            return RespDto.fail("본사 정보 수정 중 오류가 발생했습니다.");
        }
    }
}