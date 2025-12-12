package com.inc.sh.service.admin;

import com.inc.sh.dto.user.reqDto.AdminUserReqDto;
import com.inc.sh.dto.user.respDto.AdminUserDetailRespDto;
import com.inc.sh.dto.user.respDto.AdminUserListRespDto;
import com.inc.sh.dto.user.respDto.AdminUserRespDto;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.entity.Role;
import com.inc.sh.entity.User;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.MenuInfoRepository;
import com.inc.sh.repository.RoleMenuPermissionsRepository;
import com.inc.sh.repository.RoleRepository;
import com.inc.sh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {
    
    private final UserRepository userRepository;  // 관리자
    private final HeadquarterRepository headquarterRepository;
    private final RoleRepository roleRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 관리자 - 사용자 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<AdminUserListRespDto>> getUserList(Integer hqCode) {
        try {
            String searchType = (hqCode == null) ? "전체" : "본사별";
            log.info("관리자 사용자 목록 조회 시작 - 조회타입: {}, hqCode: {}", searchType, hqCode);
            
            List<Object[]> results = userRepository.findUsersForAdmin(hqCode);
            
            List<AdminUserListRespDto> responseList = results.stream()
                    .map(result -> {
                        // 사용여부 판단 (퇴사일자가 null이면 재직중)
                        String resignationDt = (String) result[9];
                        String workStatus = (resignationDt == null || resignationDt.trim().isEmpty()) ? "사용중" : "사용안함";
                        
                        return AdminUserListRespDto.builder()
                                .userCode((String) result[0])
                                .userName((String) result[1])
                                .hqCode((Integer) result[2])
                                .hqName((String) result[3])
                                .roleCode((Integer) result[4])
                                .roleName((String) result[5])
                                .phone1((String) result[6])
                                .phone2((String) result[7])
                                .email((String) result[8])
                                .workStatus(workStatus)
                                .resignationDt(resignationDt)
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("관리자 사용자 목록 조회 완료 - 조회타입: {}, hqCode: {}, 조회 건수: {}", 
                    searchType, hqCode, responseList.size());
            
            String message = String.format("사용자 목록 조회 성공 (%s)", searchType);
            return RespDto.success(message, responseList);
            
        } catch (Exception e) {
            log.error("관리자 사용자 목록 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("사용자 목록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 관리자 - 사용자 상세 조회 (List<Object[]> 처리)
     */
    @Transactional(readOnly = true)
    public RespDto<AdminUserDetailRespDto> getUserDetail(String userCode) {
        try {
            log.info("관리자 사용자 상세 조회 시작 - userCode: {}", userCode);
            
            List<Object[]> results = userRepository.findUserDetailForAdmin(userCode);
            if (results == null || results.isEmpty()) {
                return RespDto.fail("존재하지 않는 사용자입니다.");
            }
            
            Object[] result = results.get(0);  // 첫 번째 (유일한) 결과 가져오기
            
            // 배열 길이 검증
            log.info("쿼리 결과 배열 길이: {}", result.length);
            if (result.length < 12) {
                log.error("쿼리 결과 배열 길이 부족 - 예상: 12, 실제: {}", result.length);
                return RespDto.fail("사용자 조회 결과가 올바르지 않습니다.");
            }
            
            // 각 필드 안전하게 접근
            String userCodeResult = result[0] != null ? result[0].toString() : null;
            String userName = result[1] != null ? result[1].toString() : null;
            Integer hqCode = result[2] != null ? (Integer) result[2] : null;
            String hqName = result[3] != null ? result[3].toString() : null;
            Integer roleCode = result[4] != null ? (Integer) result[4] : null;
            String roleName = result[5] != null ? result[5].toString() : null;
            String phone1 = result[6] != null ? result[6].toString() : null;
            String phone2 = result[7] != null ? result[7].toString() : null;
            String email = result[8] != null ? result[8].toString() : null;
            String resignationDt = result[9] != null ? result[9].toString() : null;
            
            // 사용여부 판단
            String workStatus = (resignationDt == null || resignationDt.trim().isEmpty()) ? "사용중" : "사용안함";
            
            // Timestamp 안전 변환
            LocalDateTime createdAt = null;
            LocalDateTime updatedAt = null;
            
            if (result[10] != null && result[10] instanceof java.sql.Timestamp) {
                createdAt = ((java.sql.Timestamp) result[10]).toLocalDateTime();
            }
            if (result[11] != null && result[11] instanceof java.sql.Timestamp) {
                updatedAt = ((java.sql.Timestamp) result[11]).toLocalDateTime();
            }
            
            AdminUserDetailRespDto responseData = AdminUserDetailRespDto.builder()
                    .userCode(userCodeResult)
                    .userName(userName)
                    .hqCode(hqCode)
                    .hqName(hqName)
                    .roleCode(roleCode)
                    .roleName(roleName)
                    .phone1(phone1)
                    .phone2(phone2)
                    .email(email)
                    .workStatus(workStatus)
                    .resignationDt(resignationDt)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
            
            log.info("관리자 사용자 상세 조회 완료 - userCode: {}, userName: {}", userCodeResult, userName);
            
            return RespDto.success("사용자 상세 조회 성공", responseData);
            
        } catch (Exception e) {
            log.error("관리자 사용자 상세 조회 중 오류 발생 - userCode: {}", userCode, e);
            return RespDto.fail("사용자 상세 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 관리자 - 사용자 저장 (생성/수정 통합)
     */
    @Transactional
    public RespDto<AdminUserRespDto> saveUser(AdminUserReqDto request) {
        try {
            if (request.getUserCode() == null) {
                // 신규 사용자 생성
                return createUser(request);
            } else {
                // 기존 사용자 수정
                return updateUser(request);
            }
        } catch (Exception e) {
            log.error("관리자 사용자 저장 중 오류 발생 - userCode: {}", request.getUserCode(), e);
            return RespDto.fail("사용자 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 관리자 - 사용자 생성
     */
    private RespDto<AdminUserRespDto> createUser(AdminUserReqDto request) {
        log.info("관리자 사용자 생성 시작 - userName: {}, hqCode: {}", request.getUserName(), request.getHqCode());
        
        // 비밀번호 필수 검증
        if (request.getUserPw() == null || request.getUserPw().trim().isEmpty()) {
            return RespDto.fail("신규 사용자 생성 시 비밀번호는 필수입니다.");
        }
        
        // 본사 존재 확인
        Headquarter hq = headquarterRepository.findById(request.getHqCode()).orElse(null);
        if (hq == null) {
            return RespDto.fail("존재하지 않는 본사입니다.");
        }
        
        // 해당 본사의 관리자 권한 찾기
        Role adminRole = roleRepository.findByHqCodeAndRoleName(request.getHqCode(), "관리자");
        if (adminRole == null) {
            return RespDto.fail("해당 본사에 관리자 권한이 존재하지 않습니다.");
        }
        
        // 사용자 코드 생성
        String userCode = generateUserCode(request.getHqCode());
        
        // 사용자 생성
        User newUser = User.builder()
                .userCode(userCode)
                .hqCode(request.getHqCode())
                .roleCode(adminRole.getRoleCode())
                .userName(request.getUserName())
                .userPw(passwordEncoder.encode(request.getUserPw()))
                .phone1(request.getPhone1())
                .email(request.getEmail())
                .description("관리자가 생성한 사용자")
                .build();
        
        User savedUser = userRepository.save(newUser);
        
        AdminUserRespDto responseData = AdminUserRespDto.builder()
                .userCode(savedUser.getUserCode())
                .userName(savedUser.getUserName())
                .hqCode(savedUser.getHqCode())
                .hqName(hq.getCompanyName())
                .roleCode(savedUser.getRoleCode())
                .roleName(adminRole.getRoleName())
                .phone1(savedUser.getPhone1())
                .email(savedUser.getEmail())
                .build();
        
        log.info("관리자 사용자 생성 완료 - userCode: {}, userName: {}", userCode, request.getUserName());
        
        return RespDto.success("사용자가 성공적으로 생성되었습니다.", responseData);
    }

    /**
     * 관리자 - 사용자 수정
     */
    private RespDto<AdminUserRespDto> updateUser(AdminUserReqDto request) {
        log.info("관리자 사용자 수정 시작 - userCode: {}, userName: {}", request.getUserCode(), request.getUserName());
        
        // 기존 사용자 조회
        User existingUser = userRepository.findByUserCode(request.getUserCode());
        if (existingUser == null) {
            return RespDto.fail("존재하지 않는 사용자입니다.");
        }
        
        // 본사 정보 조회
        Headquarter hq = headquarterRepository.findById(request.getHqCode()).orElse(null);
        if (hq == null) {
            return RespDto.fail("존재하지 않는 본사입니다.");
        }
        
        // 권한 정보 조회 (기존 roleCode 유지)
        Role role = roleRepository.findByRoleCode(existingUser.getRoleCode());
        
        // 사용자 정보 업데이트
        existingUser.setHqCode(request.getHqCode());
        existingUser.setUserName(request.getUserName());
        existingUser.setPhone1(request.getPhone1());
        existingUser.setEmail(request.getEmail());
        
        // 비밀번호가 제공된 경우만 업데이트
        if (request.getUserPw() != null && !request.getUserPw().trim().isEmpty()) {
            existingUser.setUserPw(passwordEncoder.encode(request.getUserPw()));
        }
        
        User savedUser = userRepository.save(existingUser);
        
        AdminUserRespDto responseData = AdminUserRespDto.builder()
                .userCode(savedUser.getUserCode())
                .userName(savedUser.getUserName())
                .hqCode(savedUser.getHqCode())
                .hqName(hq.getCompanyName())
                .roleCode(savedUser.getRoleCode())
                .roleName(role != null ? role.getRoleName() : null)
                .phone1(savedUser.getPhone1())
                .email(savedUser.getEmail())
                .build();
        
        log.info("관리자 사용자 수정 완료 - userCode: {}, userName: {}", request.getUserCode(), request.getUserName());
        
        return RespDto.success("사용자 정보가 성공적으로 수정되었습니다.", responseData);
    }

    /**
     * 관리자 - 사용자 삭제
     */
    @Transactional
    public RespDto<String> deleteUser(String userCode) {
        try {
            log.info("관리자 사용자 삭제 시작 - userCode: {}", userCode);
            
            User user = userRepository.findByUserCode(userCode);
            if (user == null) {
                return RespDto.fail("존재하지 않는 사용자입니다.");
            }
            
            String userName = user.getUserName();
            userRepository.delete(user);
            
            log.info("관리자 사용자 삭제 완료 - userCode: {}, userName: {}", userCode, userName);
            
            String resultMessage = String.format("사용자 '%s'가 성공적으로 삭제되었습니다.", userName);
            return RespDto.success(resultMessage, "삭제완료");
            
        } catch (Exception e) {
            log.error("관리자 사용자 삭제 중 오류 발생 - userCode: {}", userCode, e);
            return RespDto.fail("사용자 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 사용자 코드 생성 (YYMM + hqCode + 001 형태)
     */
    private String generateUserCode(Integer hqCode) {
        // 현재 년월 (YYMM)
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        
        // 해당 년월+본사코드의 마지막 사번 조회
        String prefix = yearMonth + hqCode.toString();
        String lastUserCode = userRepository.findLastUserCodeByPrefix(prefix);
        
        int sequence = 1;
        if (lastUserCode != null && lastUserCode.startsWith(prefix)) {
            String lastSequenceStr = lastUserCode.substring(prefix.length());
            if (lastSequenceStr.length() == 3) {
                try {
                    sequence = Integer.parseInt(lastSequenceStr) + 1;
                } catch (NumberFormatException e) {
                    log.warn("사용자 코드 시퀀스 파싱 실패 - lastUserCode: {}", lastUserCode);
                    sequence = 1;
                }
            }
        }
        
        return String.format("%s%03d", prefix, sequence);
    }
}