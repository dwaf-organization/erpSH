package com.inc.sh.service.admin;

import com.inc.sh.dto.headquarter.respDto.AdminHeadquarterListRespDto;
import com.inc.sh.dto.headquarter.respDto.HeadquarterRespDto;
import com.inc.sh.dto.headquarter.reqDto.AdminHeadquarterReqDto;
import com.inc.sh.dto.headquarter.reqDto.HeadquarterReqDto;
import com.inc.sh.dto.headquarter.respDto.AdminHeadquarterDetailRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.entity.MenuInfo;
import com.inc.sh.entity.Role;
import com.inc.sh.entity.RoleMenuPermissions;
import com.inc.sh.entity.User;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.MenuInfoRepository;
import com.inc.sh.repository.RoleMenuPermissionsRepository;
import com.inc.sh.repository.RoleRepository;
import com.inc.sh.repository.UserRepository;
import com.inc.sh.util.HqAccessCodeGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminHeadquarterService {
    
    private final HeadquarterRepository headquarterRepository;  // 관리자
    private final RoleRepository roleRepository;
    private final RoleMenuPermissionsRepository roleMenuPermissionsRepository;
    private final MenuInfoRepository menuInfoRepository;
    private final UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 관리자 - 본사 목록 조회 (전체)
     */
    @Transactional(readOnly = true)
    public RespDto<List<AdminHeadquarterListRespDto>> getHeadquarterList() {
        try {
            log.info("관리자 본사 목록 조회 시작");
            
            List<Headquarter> headquarters = headquarterRepository.findAll();
            
            List<AdminHeadquarterListRespDto> responseList = headquarters.stream()
                    .map(hq -> AdminHeadquarterListRespDto.builder()
                            .hqCode(hq.getHqCode())
                            .hqAccessCode(hq.getHqAccessCode())
                            .companyName(hq.getCompanyName())
                            .corpRegNum(hq.getCorpRegNum())
                            .bizNum(hq.getBizNum())
                            .ceoName(hq.getCeoName())
                            .zipCode(hq.getZipCode())
                            .addr(hq.getAddr())
                            .bizType(hq.getBizType())
                            .bizItem(hq.getBizItem())
                            .telNum(hq.getTelNum())
                            .inquiryTelNum(hq.getInquiryTelNum())
                            .faxNum(hq.getFaxNum())
                            .homepage(hq.getHomepage())
                            .bankName(hq.getBankName())
                            .accountNum(hq.getAccountNum())
                            .accountHolder(hq.getAccountHolder())
                            .logisticsType(hq.getLogisticsType())
                            .priceDisplayType(hq.getPriceDisplayType())
                            .description(hq.getDescription())
                            .createdAt(hq.getCreatedAt())
                            .updatedAt(hq.getUpdatedAt())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("관리자 본사 목록 조회 완료 - 조회된 본사 수: {}", responseList.size());
            
            return RespDto.success("본사 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("관리자 본사 목록 조회 중 오류 발생", e);
            return RespDto.fail("본사 목록 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 관리자 - 본사 상세 조회 (개별)
     */
    @Transactional(readOnly = true)
    public RespDto<AdminHeadquarterDetailRespDto> getHeadquarterDetail(Integer hqCode) {
        try {
            log.info("관리자 본사 상세 조회 시작 - hqCode: {}", hqCode);
            
            Headquarter headquarter = headquarterRepository.findById(hqCode).orElse(null);
            if (headquarter == null) {
                return RespDto.fail("존재하지 않는 본사입니다.");
            }
            
            AdminHeadquarterDetailRespDto responseData = AdminHeadquarterDetailRespDto.builder()
                    .hqCode(headquarter.getHqCode())
                    .hqAccessCode(headquarter.getHqAccessCode())
                    .companyName(headquarter.getCompanyName())
                    .corpRegNum(headquarter.getCorpRegNum())
                    .bizNum(headquarter.getBizNum())
                    .ceoName(headquarter.getCeoName())
                    .zipCode(headquarter.getZipCode())
                    .addr(headquarter.getAddr())
                    .bizType(headquarter.getBizType())
                    .bizItem(headquarter.getBizItem())
                    .telNum(headquarter.getTelNum())
                    .inquiryTelNum(headquarter.getInquiryTelNum())
                    .faxNum(headquarter.getFaxNum())
                    .homepage(headquarter.getHomepage())
                    .bankName(headquarter.getBankName())
                    .accountNum(headquarter.getAccountNum())
                    .accountHolder(headquarter.getAccountHolder())
                    .logisticsType(headquarter.getLogisticsType())
                    .priceDisplayType(headquarter.getPriceDisplayType())
                    .description(headquarter.getDescription())
                    .createdAt(headquarter.getCreatedAt())
                    .updatedAt(headquarter.getUpdatedAt())
                    .build();
            
            log.info("관리자 본사 상세 조회 완료 - hqCode: {}, 회사명: {}", hqCode, headquarter.getCompanyName());
            
            return RespDto.success("본사 상세 조회 성공", responseData);
            
        } catch (Exception e) {
            log.error("관리자 본사 상세 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("본사 상세 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 관리자 - 본사 저장 (생성/수정 통합)
     */
    @Transactional
    public RespDto<HeadquarterRespDto> saveHeadquarterAdmin(AdminHeadquarterReqDto request) {
        try {
            if (request.getHqCode() == null) {
                // 신규 본사 생성 - 기존 로직 활용
                log.info("관리자 - 신규 본사 생성 요청 - companyName: {}", request.getCompanyName());
                
                HeadquarterReqDto createRequest = HeadquarterReqDto.builder()
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
                
                return saveHeadquarter(createRequest);  // 기존 생성 로직 호출
                
            } else {
                // 기존 본사 수정
                log.info("관리자 - 본사 수정 요청 - hqCode: {}, companyName: {}", request.getHqCode(), request.getCompanyName());
                
                return updateHeadquarter(request);  // 수정 로직 호출
            }
            
        } catch (Exception e) {
            log.error("관리자 본사 저장 중 오류 발생 - hqCode: {}", request.getHqCode(), e);
            return RespDto.fail("본사 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 관리자 - 본사 수정 (본사정보만 업데이트)
     */
    private RespDto<HeadquarterRespDto> updateHeadquarter(AdminHeadquarterReqDto request) {
        try {
            log.info("본사 수정 시작 - hqCode: {}, companyName: {}", request.getHqCode(), request.getCompanyName());
            
            // 1. 기존 본사 조회
            Headquarter existingHq = headquarterRepository.findById(request.getHqCode()).orElse(null);
            if (existingHq == null) {
                return RespDto.fail("존재하지 않는 본사입니다.");
            }
            
            // 2. 사업자번호 중복 체크 (본인 제외)
            headquarterRepository.findByBizNum(request.getBizNum())
                .ifPresent(hq -> {
                    if (!hq.getHqCode().equals(request.getHqCode())) {
                        throw new RuntimeException("이미 사용중인 사업자번호입니다: " + request.getBizNum());
                    }
                });
            
            // 3. 본사 정보 업데이트 (접속코드는 수정하지 않음)
            existingHq.setCompanyName(request.getCompanyName());
            existingHq.setCeoName(request.getCeoName());
            existingHq.setBizNum(request.getBizNum());
            existingHq.setCorpRegNum(request.getCorpRegNum());
            existingHq.setZipCode(request.getZipCode());
            existingHq.setAddr(request.getAddr());
            existingHq.setBizType(request.getBizType());
            existingHq.setBizItem(request.getBizItem());
            existingHq.setTelNum(request.getTelNum());
            existingHq.setInquiryTelNum(request.getInquiryTelNum());
            existingHq.setFaxNum(request.getFaxNum());
            existingHq.setHomepage(request.getHomepage());
            existingHq.setBankName(request.getBankName());
            existingHq.setAccountNum(request.getAccountNum());
            existingHq.setAccountHolder(request.getAccountHolder());
            
            Headquarter updatedHq = headquarterRepository.save(existingHq);
            
            log.info("본사 수정 완료 - hqCode: {}, companyName: {}", updatedHq.getHqCode(), updatedHq.getCompanyName());
            
            // 4. 응답 생성
            HeadquarterRespDto responseDto = HeadquarterRespDto.from(updatedHq);
            
            return RespDto.success("본사 정보가 성공적으로 수정되었습니다.", responseDto);
            
        } catch (RuntimeException e) {
            log.error("본사 수정 중 비즈니스 오류 발생 - hqCode: {}, 오류: {}", request.getHqCode(), e.getMessage());
            return RespDto.fail(e.getMessage());
        } catch (Exception e) {
            log.error("본사 수정 중 오류 발생 - hqCode: {}", request.getHqCode(), e);
            return RespDto.fail("본사 수정 중 오류가 발생했습니다: " + e.getMessage());
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
     * 관리자 - 본사 삭제 (권한, 권한메뉴, 사용자 함께 삭제)
     */
    @Transactional
    public RespDto<String> deleteHeadquarter(Integer hqCode) {
        try {
            log.info("관리자 본사 삭제 시작 - hqCode: {}", hqCode);
            
            // 1. 본사 존재 여부 확인
            Headquarter headquarter = headquarterRepository.findById(hqCode).orElse(null);
            if (headquarter == null) {
                return RespDto.fail("존재하지 않는 본사입니다.");
            }
            
            String companyName = headquarter.getCompanyName();
            log.info("본사 삭제 대상 확인 - hqCode: {}, companyName: {}", hqCode, companyName);
            
            // 2. 해당 본사의 권한 목록 조회
            List<Role> roles = roleRepository.findByHqCode(hqCode);
            log.info("삭제할 권한 수: {}", roles.size());
            
            for (Role role : roles) {
                Integer roleCode = role.getRoleCode();
                
                // 2-1. 권한별 메뉴 권한 삭제
                log.info("권한 메뉴 권한 삭제 - roleCode: {}", roleCode);
                roleMenuPermissionsRepository.deleteByRoleCode(roleCode);
                
                // 2-2. 해당 권한을 가진 사용자들 삭제
                List<User> users = userRepository.findByRoleCode(roleCode);
                log.info("삭제할 사용자 수 - roleCode: {}, userCount: {}", roleCode, users.size());
                
                for (User user : users) {
                    log.info("사용자 삭제 - userCode: {}, userName: {}", user.getUserCode(), user.getUserName());
                    userRepository.delete(user);
                }
                
                // 2-3. 권한 삭제
                log.info("권한 삭제 - roleCode: {}, roleName: {}", roleCode, role.getRoleName());
                roleRepository.delete(role);
            }
            
            // 3. 본사 삭제
            log.info("본사 삭제 - hqCode: {}, companyName: {}", hqCode, companyName);
            headquarterRepository.delete(headquarter);
            
            log.info("관리자 본사 삭제 완료 - hqCode: {}, companyName: {}", hqCode, companyName);
            
            String resultMessage = String.format("본사 '%s'가 성공적으로 삭제되었습니다. (권한: %d개, 관련 데이터 모두 삭제)", 
                    companyName, roles.size());
            
            return RespDto.success(resultMessage, "삭제완료");
            
        } catch (Exception e) {
            log.error("관리자 본사 삭제 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("본사 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
}