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
    private final RoleRepository roleRepository;
    private final RoleMenuPermissionsRepository roleMenuPermissionsRepository;
    private final MenuInfoRepository menuInfoRepository;
    private final UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
     * 본사 등록 (관리자 권한/사용자 자동 생성 포함)
     */
    @Transactional
    public RespDto<HeadquarterRespDto> saveHeadquarter(HeadquarterReqDto request) {
        
        try {
            log.info("본사 등록 시작 - companyName: {}, bizNum: {}", request.getCompanyName(), request.getBizNum());
            
            // 1. 사업자번호 중복 체크
            if (headquarterRepository.findByBizNum(request.getBizNum()).isPresent()) {
                log.warn("이미 등록된 사업자번호입니다 - bizNum: {}", request.getBizNum());
                return RespDto.fail("이미 등록된 사업자번호입니다: " + request.getBizNum());
            }
            
            // 2. 본사접속코드 생성 (중복 체크)
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

            // 3. 본사 Entity 생성 및 저장
            Headquarter headquarter = Headquarter.builder()
                    .hqAccessCode(hqAccessCode)
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

            Headquarter savedHeadquarter = headquarterRepository.save(headquarter);
            Integer hqCode = savedHeadquarter.getHqCode();
            
            log.info("본사 저장 완료 - hqCode: {}, companyName: {}", hqCode, savedHeadquarter.getCompanyName());
            
            // 4. 관리자 권한(Role) 생성
            Role adminRole = Role.builder()
                    .hqCode(hqCode)
                    .roleName("관리자")
                    .note("본사 등록시 자동 생성된 관리자 권한")
                    .description("관리자권한생성")
                    .build();
            
            Role savedRole = roleRepository.save(adminRole);
            Integer roleCode = savedRole.getRoleCode();
            
            log.info("관리자 권한 생성 완료 - roleCode: {}, roleName: {}", roleCode, savedRole.getRoleName());
            
            // 5. 모든 메뉴에 대한 권한 생성
            List<MenuInfo> allMenus = menuInfoRepository.findAll();
            
            for (MenuInfo menu : allMenus) {
                RoleMenuPermissions permission = RoleMenuPermissions.builder()
                        .menuCode(menu.getMenuCode())
                        .roleCode(roleCode)
                        .canView(true)
                        .description("관리자 권한 자동 생성")
                        .build();
                
                roleMenuPermissionsRepository.save(permission);
            }
            
            log.info("메뉴 권한 생성 완료 - 총 {}개 메뉴", allMenus.size());
            
            // 6. 관리자 사용자 생성
            String adminUserCode = generateAdminUserCode(hqCode);
            String adminPassword = adminUserCode; // 사번과 동일한 패스워드
            
            User adminUser = User.builder()
                    .userCode(adminUserCode)
                    .hqCode(hqCode)
                    .roleCode(roleCode)
                    .userName("관리자")
                    .userPw(passwordEncoder.encode(adminPassword))
                    .phone1("000-0000-0000")
                    .email("admin@" + request.getCompanyName().toLowerCase() + ".com")
                    .description("본사등록시 자동 생성된 관리자")
                    .build();
            
            User savedUser = userRepository.save(adminUser);
            
            log.info("관리자 사용자 생성 완료 - userCode: {}, userName: {}", 
                    savedUser.getUserCode(), savedUser.getUserName());
            
            // 7. 응답 생성
            HeadquarterRespDto responseDto = HeadquarterRespDto.from(savedHeadquarter);
            
            log.info("본사 등록 전체 완료 - hqCode: {}, adminUserCode: {}, adminRoleCode: {}", 
                    hqCode, adminUserCode, roleCode);
            
            return RespDto.success("본사가 성공적으로 등록되었습니다.", responseDto);
            
        } catch (Exception e) {
            log.error("본사 등록 중 오류 발생", e);
            return RespDto.fail("본사 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 사용자 코드 생성 (YYMM + hq_code + 001 형태)
     */
    private String generateAdminUserCode(Integer hqCode) {
        // 현재 년월 (YYMM)
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        
        // 해당 년월+본사코드의 마지막 사번 조회
        String prefix = yearMonth + hqCode.toString();
        String lastUserCode = userRepository.findLastUserCodeByPrefix(prefix);
        
        int sequence = 1;
        if (lastUserCode != null && lastUserCode.startsWith(prefix)) {
            // 마지막 3자리 추출하여 다음 순번 계산
            String lastSequenceStr = lastUserCode.substring(prefix.length());
            if (lastSequenceStr.length() == 3) {
                try {
                    sequence = Integer.parseInt(lastSequenceStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("관리자 사번 순번 파싱 실패, 기본값 1 사용 - lastUserCode: {}", lastUserCode);
                    sequence = 1;
                }
            }
        }
        
        String adminUserCode = String.format("%s%03d", prefix, sequence);
        log.info("관리자 사번 생성 완료 - hqCode: {}, prefix: {}, sequence: {}, userCode: {}", 
                hqCode, prefix, sequence, adminUserCode);
        
        return adminUserCode;
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