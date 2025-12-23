package com.inc.sh.service;

import com.inc.sh.dto.user.reqDto.UserSearchDto;
import com.inc.sh.dto.user.reqDto.UserDeleteReqDto;
import com.inc.sh.dto.user.reqDto.UserSaveDto;
import com.inc.sh.dto.user.reqDto.UserSaveReqDto;
import com.inc.sh.dto.user.respDto.UserBatchResult;
import com.inc.sh.dto.user.respDto.UserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.User;
import com.inc.sh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 사용자 조회 (hqCode 검증 추가)
     */
    @Transactional(readOnly = true)
    public RespDto<List<UserRespDto>> getUserList(UserSearchDto searchDto) {
        try {
            log.info("사용자 조회 시작 - hqCode: {}, userCode: {}, userName: {}", 
                    searchDto.getHqCode(), searchDto.getUserCode(), searchDto.getUserName());
            
            // hqCode 필수 검증
            if (searchDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수 파라미터입니다.");
            }
            
            // hqCode 포함 조회
            List<Object[]> results = userRepository.findUsersWithRoleByConditionsAndHqCode(
                    searchDto.getHqCode(),
                    searchDto.getUserCode(),
                    searchDto.getUserName()
            );
            
            List<UserRespDto> responseList = results.stream()
                    .map(result -> UserRespDto.builder()
                            .userCode((String) result[0])
                            .userName((String) result[1])
                            .phone1((String) result[2])
                            .phone2((String) result[3])
                            .email((String) result[4])
                            .resignationDt(formatResignationDate((String) result[5]))
                            .roleCode((Integer) result[6])
                            .roleName((String) result[7])
                            .userPw(null)
                            .ledgerUsageYn((Integer) result[9])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("사용자 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("사용자 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생", e);
            return RespDto.fail("사용자 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<UserBatchResult> saveUsers(UserSaveReqDto request) {
        try {
            log.info("사용자 다중 저장 시작 - 총 {}건", 
                    request.getUsers() != null ? request.getUsers().size() : 0);
            
            // 요청 데이터 검증
            if (request.getUsers() == null || request.getUsers().isEmpty()) {
                return RespDto.fail("저장할 사용자 데이터가 없습니다.");
            }
            
            List<UserBatchResult.UserSuccessResult> successList = new ArrayList<>();
            List<UserBatchResult.UserFailureResult> failureList = new ArrayList<>();
            
            // 개별 저장 처리
            for (UserSaveReqDto.UserSaveItemDto saveDto : request.getUsers()) {
                try {
                    // 필수 필드 검증
                    if (saveDto.getUserName() == null || saveDto.getUserName().trim().isEmpty()) {
                        throw new RuntimeException("성명은 필수입니다.");
                    }
                    if (saveDto.getHqCode() == null) {
                        throw new RuntimeException("본사코드는 필수입니다.");
                    }
                    if (saveDto.getRoleCode() == null) {
                        throw new RuntimeException("사용자권한코드는 필수입니다.");
                    }
                    
                    UserBatchResult.UserSuccessResult result = saveSingleUser(saveDto);
                    if (result != null) {
                        successList.add(result);
                        log.info("사용자 저장 성공 - 사번: {}, 성명: {}", result.getUserCode(), result.getUserName());
                    }
                } catch (Exception e) {
                    UserBatchResult.UserFailureResult failure = 
                        UserBatchResult.UserFailureResult.builder()
                                .userCode(saveDto.getUserCode())
                                .userName(saveDto.getUserName())
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("사용자 저장 실패 - 성명: {}, 원인: {}", saveDto.getUserName(), e.getMessage());
                }
            }
            
            // 결과 집계
            UserBatchResult batchResult = UserBatchResult.builder()
                    .totalCount(request.getUsers().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("사용자 다중 저장 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("사용자 다중 저장 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("사용자 다중 저장 중 오류 발생", e);
            return RespDto.fail("사용자 다중 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 사용자 다중 삭제
     */
    @Transactional
    public RespDto<UserBatchResult> deleteUsers(UserDeleteReqDto request) {
        try {
            log.info("사용자 다중 삭제 시작 - 총 {}건", 
                    request.getUserCodes() != null ? request.getUserCodes().size() : 0);
            
            // 요청 데이터 검증
            if (request.getUserCodes() == null || request.getUserCodes().isEmpty()) {
                return RespDto.fail("삭제할 사번이 없습니다.");
            }
            
            List<UserBatchResult.UserSuccessResult> successList = new ArrayList<>();
            List<UserBatchResult.UserFailureResult> failureList = new ArrayList<>();
            
            // 개별 삭제 처리
            for (String userCode : request.getUserCodes()) {
                try {
                    UserBatchResult.UserSuccessResult result = deleteSingleUser(userCode);
                    if (result != null) {
                        successList.add(result);
                        log.info("사용자 삭제 성공 - 사번: {}", userCode);
                    }
                } catch (Exception e) {
                    UserBatchResult.UserFailureResult failure = 
                        UserBatchResult.UserFailureResult.builder()
                                .userCode(userCode)
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("사용자 삭제 실패 - 사번: {}, 원인: {}", userCode, e.getMessage());
                }
            }
            
            // 결과 집계
            UserBatchResult batchResult = UserBatchResult.builder()
                    .totalCount(request.getUserCodes().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("사용자 다중 삭제 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("사용자 다중 삭제 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("사용자 다중 삭제 중 오류 발생", e);
            return RespDto.fail("사용자 다중 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 개별 사용자 저장 처리
     */
    private UserBatchResult.UserSuccessResult saveSingleUser(UserSaveReqDto.UserSaveItemDto saveDto) {
        boolean isUpdate = saveDto.getUserCode() != null && !saveDto.getUserCode().isEmpty();
        User user;
        
        if (isUpdate) {
            // 수정
            user = userRepository.findByUserCode(saveDto.getUserCode());
            if (user == null) {
                throw new RuntimeException("해당 사용자를 찾을 수 없습니다.");
            }
            
            user.setUserName(saveDto.getUserName());
            user.setEmail(saveDto.getEmail());
            user.setPhone1(saveDto.getPhone1());
            user.setPhone2(saveDto.getPhone2());
            user.setHqCode(saveDto.getHqCode());
            user.setRoleCode(saveDto.getRoleCode());
            user.setResignationDt(saveDto.getResignationDt());
            
            // 장부대장 사용여부 업데이트 (받은 값 사용, null이면 기본값 0)
            user.setLedgerUsageYn(saveDto.getLedgerUsageYn() != null ? saveDto.getLedgerUsageYn() : 0);
            
            // 비밀번호가 제공된 경우에만 암호화하여 업데이트
            if (saveDto.getUserPw() != null && !saveDto.getUserPw().isEmpty()) {
                user.setUserPw(passwordEncoder.encode(saveDto.getUserPw()));
            }
            user.setDescription("사용자수정");
            
        } else {
            // 신규 - 사번 자동 생성 (hqCode 기반)
            String newUserCode = generateUserCodeWithHqCode(saveDto.getHqCode());
            
            // 비밀번호 필수 검증 (신규 시)
            if (saveDto.getUserPw() == null || saveDto.getUserPw().trim().isEmpty()) {
                throw new RuntimeException("신규 사용자는 비밀번호가 필수입니다.");
            }
            
            user = User.builder()
                    .userCode(newUserCode)
                    .hqCode(saveDto.getHqCode())
                    .roleCode(saveDto.getRoleCode())
                    .userName(saveDto.getUserName())
                    .userPw(passwordEncoder.encode(saveDto.getUserPw())) // 비밀번호 암호화
                    .phone1(saveDto.getPhone1())
                    .phone2(saveDto.getPhone2())
                    .email(saveDto.getEmail())
                    .resignationDt(null) // 신규 생성 시 null
                    .ledgerUsageYn(saveDto.getLedgerUsageYn() != null ? saveDto.getLedgerUsageYn() : 0)
                    .description("사용자등록")
                    .build();
        }
        
        user = userRepository.save(user);
        
        String action = isUpdate ? "수정" : "등록";
        return UserBatchResult.UserSuccessResult.builder()
                .userCode(user.getUserCode())
                .userName(user.getUserName())
                .hqCode(user.getHqCode())
                .roleCode(user.getRoleCode())
                .roleName(null) // 권한명은 별도 조회 필요하면 추가
                .message(String.format("%s 완료", action))
                .build();
    }
    
    /**
     * 개별 사용자 삭제 처리
     */
    private UserBatchResult.UserSuccessResult deleteSingleUser(String userCode) {
        User user = userRepository.findByUserCode(userCode);
        if (user == null) {
            throw new RuntimeException("해당 사용자를 찾을 수 없습니다.");
        }
        
        String userName = user.getUserName();
        Integer hqCode = user.getHqCode();
        Integer roleCode = user.getRoleCode();
        
        userRepository.delete(user);
        
        return UserBatchResult.UserSuccessResult.builder()
                .userCode(userCode)
                .userName(userName)
                .hqCode(hqCode)
                .roleCode(roleCode)
                .roleName(null)
                .message("삭제 완료")
                .build();
    }
    
    /**
     * 사번 자동 생성 (YYMM + hqCode + 001 형태)
     */
    private String generateUserCodeWithHqCode(Integer hqCode) {
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
                    log.warn("사번 순번 파싱 실패, 기본값 1 사용 - lastUserCode: {}", lastUserCode);
                    sequence = 1;
                }
            }
        }
        
        String newUserCode = String.format("%s%03d", prefix, sequence);
        log.info("사번 생성 완료 - hqCode: {}, prefix: {}, sequence: {}, userCode: {}", 
                hqCode, prefix, sequence, newUserCode);
        
        return newUserCode;
    }
    
    /**
     * 기존 generateUserCode 메서드 (기본 본사코드 1 사용)
     * 하위 호환성을 위해 유지
     */
    private String generateUserCode() {
        return generateUserCodeWithHqCode(1);
    }
    
    /**
     * 퇴사일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatResignationDate(String resignationDt) {
        try {
            if (resignationDt != null && resignationDt.length() == 8) {
                LocalDate date = LocalDate.parse(resignationDt, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return resignationDt;
        } catch (Exception e) {
            log.warn("퇴사일자 포맷 변환 실패: {}", resignationDt);
            return resignationDt;
        }
    }
    
    /**
     * 장부대장 사용여부 조회
     */
    @Transactional(readOnly = true)
    public RespDto<Integer> getLedgerUsage(Integer hqCode, String userCode) {
        try {
            log.info("장부대장 사용여부 조회 시작 - hqCode: {}, userCode: {}", hqCode, userCode);
            
            // 본사코드와 사번으로 사용자 조회
            User user = userRepository.findByHqCodeAndUserCode(hqCode, userCode);
            if (user == null) {
                return RespDto.fail("해당 사용자를 찾을 수 없습니다.");
            }
            
            Integer ledgerUsageYn = user.getLedgerUsageYn();
            
            log.info("장부대장 사용여부 조회 완료 - hqCode: {}, userCode: {}, ledgerUsageYn: {}", 
                    hqCode, userCode, ledgerUsageYn);
            
            return RespDto.success("장부대장 사용여부 조회 성공", ledgerUsageYn);
            
        } catch (Exception e) {
            log.error("장부대장 사용여부 조회 중 오류 발생 - hqCode: {}, userCode: {}", hqCode, userCode, e);
            return RespDto.fail("장부대장 사용여부 조회 중 오류가 발생했습니다.");
        }
    }
}