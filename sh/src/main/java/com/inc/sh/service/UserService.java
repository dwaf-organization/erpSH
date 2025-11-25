package com.inc.sh.service;

import com.inc.sh.dto.user.reqDto.UserSearchDto;
import com.inc.sh.dto.user.reqDto.UserSaveDto;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 사용자 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<UserRespDto>> getUserList(UserSearchDto searchDto) {
        try {
            log.info("사용자 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = userRepository.findUsersWithRoleByConditions(
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
                            .userPw((String) result[8]) // 조회 시에도 표시
                            .build())
                    .collect(Collectors.toList());
            
            log.info("사용자 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("사용자 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생", e);
            return RespDto.fail("사용자 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 사용자 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveUser(UserSaveDto saveDto) {
        try {
            log.info("사용자 저장 시작 - 사번: {}, 성명: {}", saveDto.getUserCode(), saveDto.getUserName());
            
            boolean isUpdate = saveDto.getUserCode() != null && !saveDto.getUserCode().isEmpty();
            User user;
            
            if (isUpdate) {
                // 수정
                user = userRepository.findByUserCode(saveDto.getUserCode());
                if (user == null) {
                    return RespDto.fail("해당 사용자를 찾을 수 없습니다.");
                }
                
                user.setUserName(saveDto.getUserName());
                user.setEmail(saveDto.getEmail());
                user.setPhone1(saveDto.getPhone1());
                user.setPhone2(saveDto.getPhone2());
                user.setHqCode(saveDto.getHqCode());
                user.setRoleCode(saveDto.getRoleCode());
                user.setResignationDt(saveDto.getResignationDt());
                
                // 비밀번호가 제공된 경우에만 암호화하여 업데이트
                if (saveDto.getUserPw() != null && !saveDto.getUserPw().isEmpty()) {
                    user.setUserPw(passwordEncoder.encode(saveDto.getUserPw()));
                }
                user.setDescription("사용자수정");
                
            } else {
                // 신규 - 사번 자동 생성
                String newUserCode = generateUserCode();
                
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
                        .description("사용자등록")
                        .build();
            }
            
            user = userRepository.save(user);
            
            String action = isUpdate ? "수정" : "등록";
            log.info("사용자 {} 완료 - 사번: {}", action, user.getUserCode());
            return RespDto.success("사용자가 " + action + "되었습니다.", user.getUserCode());
            
        } catch (Exception e) {
            log.error("사용자 저장 중 오류 발생", e);
            return RespDto.fail("사용자 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 사용자 삭제 (하드 딜리트)
     */
    @Transactional
    public RespDto<String> deleteUser(String userCode) {
        try {
            log.info("사용자 삭제 시작 - 사번: {}", userCode);
            
            User user = userRepository.findByUserCode(userCode);
            if (user == null) {
                return RespDto.fail("해당 사용자를 찾을 수 없습니다.");
            }
            
            userRepository.delete(user);
            
            log.info("사용자 삭제 완료 - 사번: {}", userCode);
            return RespDto.success("사용자가 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("사용자 삭제 중 오류 발생", e);
            return RespDto.fail("사용자 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 사번 자동 생성 (2511001 형태)
     */
    private String generateUserCode() {
        // 현재 년월 (YYMM)
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));
        
        // 해당 년월의 마지막 사번 조회
        String lastUserCode = userRepository.findLastUserCodeByYearMonth(yearMonth);
        
        int sequence = 1;
        if (lastUserCode != null && lastUserCode.length() == 7) {
            // 2511001에서 001 부분 추출
            String lastSequenceStr = lastUserCode.substring(4);
            sequence = Integer.parseInt(lastSequenceStr) + 1;
        }
        
        return String.format("%s%03d", yearMonth, sequence);
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
}