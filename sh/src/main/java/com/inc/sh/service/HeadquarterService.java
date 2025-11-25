package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.reqDto.SiteInfoReqDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.dto.headquarter.respDto.SiteInfoRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.util.HqAccessCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 본사 등록
     */
    @Transactional
    public RespDto<HeadquarterRespDto> saveHeadquarter(HeadquarterReqDto request) {
        
        try {
            log.info("본사 등록 시작 - companyName: {}, bizNum: {}", request.getCompanyName(), request.getBizNum());
            
            // 사업자번호 중복 체크
            if (headquarterRepository.findByBizNum(request.getBizNum()).isPresent()) {
                log.warn("이미 등록된 사업자번호입니다 - bizNum: {}", request.getBizNum());
                return RespDto.fail("이미 등록된 사업자번호입니다: " + request.getBizNum());
            }
            
            // 본사접속코드 생성 (중복 체크)
            String hqAccessCode;
            int retryCount = 0;
            do {
                hqAccessCode = HqAccessCodeGenerator.generateHqAccessCode();
                retryCount++;
                if (retryCount > 10) {
                    log.error("본사접속코드 생성 실패 - 너무 많은 재시도");
                    return RespDto.fail("본사접속코드 생성 중 오류가 발생했습니다.");
                }
            } while (headquarterRepository.existsByHqAccessCode(hqAccessCode));
            
            log.info("본사접속코드 생성 완료 - hqAccessCode: {}", hqAccessCode);

            // Entity 생성
            Headquarter headquarter = Headquarter.builder()
                    .hqAccessCode(hqAccessCode) // 접속코드 추가
                    .companyName(request.getCompanyName())
                    .ceoName(request.getCeoName())
                    .bizNum(request.getBizNum())
                    .corpRegNum(request.getCorpRegNum())
                    .zipCode(request.getZipCode())
                    .addr(request.getAddr())
                    .bizType(request.getBizType())
                    .bizItem(request.getBizItem())
                    .telNum(request.getTelNum())
                    .inquiryTelNum(request.getInquiryTelNum())
                    .faxNum(request.getFaxNum())
                    .homepage(request.getHomepage())
                    .bankName(request.getBankName())
                    .accountNum(request.getAccountNum())
                    .accountHolder(request.getAccountHolder())
                    .build();

            // 저장
            Headquarter saved = headquarterRepository.save(headquarter);
            
            log.info("본사 등록 완료 - hqCode: {}, hqAccessCode: {}, companyName: {}", 
                    saved.getHqCode(), hqAccessCode, saved.getCompanyName());
            
            return RespDto.success("본사가 성공적으로 등록되었습니다.", HeadquarterRespDto.from(saved));
            
        } catch (Exception e) {
            log.error("본사 등록 중 오류 발생", e);
            return RespDto.fail("본사 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 본사 정보 수정
     * @param request 본사 정보 수정 요청
     * @return 수정된 본사 정보
     */
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