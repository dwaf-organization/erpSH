package com.inc.sh.service;

import com.inc.sh.dto.customerUser.reqDto.CustomerUserSearchDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserSaveDto;
import com.inc.sh.dto.customerUser.respDto.CustomerUserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.CustomerUser;
import com.inc.sh.repository.CustomerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerUserService {
    
    private final CustomerUserRepository customerUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 거래처사용자 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerUserRespDto>> getCustomerUserList(CustomerUserSearchDto searchDto) {
        try {
            log.info("거래처사용자 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = customerUserRepository.findCustomerUsersWithCustomerByConditions(
                    searchDto.getCustomerCode(),
                    searchDto.getCustomerUserId()
            );
            
            List<CustomerUserRespDto> responseList = results.stream()
                    .map(result -> CustomerUserRespDto.builder()
                            .customerCode((Integer) result[0])
                            .customerName((String) result[1])
                            .customerUserCode((Integer) result[2])
                            .customerUserId((String) result[3])
                            .customerUserName((String) result[4])
                            .contactNum((String) result[5])
                            .email((String) result[6])
                            .endYn((Integer) result[7])
                            .customerUserPw((String) result[8]) // 조회 시에도 표시
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처사용자 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처사용자 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처사용자 조회 중 오류 발생", e);
            return RespDto.fail("거래처사용자 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처사용자 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveCustomerUser(CustomerUserSaveDto saveDto) {
        try {
            log.info("거래처사용자 저장 시작 - 사용자코드: {}, 아이디: {}", saveDto.getCustomerUserCode(), saveDto.getCustomerUserId());
            
            boolean isUpdate = saveDto.getCustomerUserCode() != null;
            CustomerUser customerUser;
            
            if (isUpdate) {
                // 수정
                customerUser = customerUserRepository.findByCustomerUserCode(saveDto.getCustomerUserCode());
                if (customerUser == null) {
                    return RespDto.fail("해당 거래처사용자를 찾을 수 없습니다.");
                }
                
                // 아이디 중복 체크 (본인 제외)
                CustomerUser existingUser = customerUserRepository.findByCustomerUserId(saveDto.getCustomerUserId());
                if (existingUser != null && !existingUser.getCustomerUserCode().equals(saveDto.getCustomerUserCode())) {
                    return RespDto.fail("이미 사용중인 아이디입니다.");
                }
                
                customerUser.setCustomerCode(saveDto.getCustomerCode());
                customerUser.setCustomerUserId(saveDto.getCustomerUserId());
                customerUser.setCustomerUserName(saveDto.getCustomerUserName());
                customerUser.setContactNum(saveDto.getContactNum());
                customerUser.setEmail(saveDto.getEmail());
                customerUser.setEndYn(saveDto.getEndYn());
                
                // 비밀번호가 제공된 경우에만 암호화하여 업데이트
                if (saveDto.getCustomerUserPw() != null && !saveDto.getCustomerUserPw().isEmpty()) {
                    customerUser.setCustomerUserPw(passwordEncoder.encode(saveDto.getCustomerUserPw()));
                }
                
                // 거래처가 변경된 경우 가상계좌코드 다시 설정
                String virtualAccount = customerUserRepository.findVirtualAccountByCustomerCode(saveDto.getCustomerCode());
                if (virtualAccount != null && !virtualAccount.isEmpty()) {
                    try {
                        customerUser.setVirtualAccountCode(Integer.valueOf(virtualAccount));
                    } catch (NumberFormatException e) {
                        customerUser.setVirtualAccountCode(null);
                    }
                } else {
                    customerUser.setVirtualAccountCode(null);
                }
                
                customerUser.setDescription("거래처사용자수정");
                
            } else {
                // 신규 - 아이디 중복 체크
                if (customerUserRepository.existsByCustomerUserId(saveDto.getCustomerUserId())) {
                    return RespDto.fail("이미 사용중인 아이디입니다.");
                }
                
                // 거래처의 가상계좌 조회
                Integer virtualAccountCode = null;
                String virtualAccount = customerUserRepository.findVirtualAccountByCustomerCode(saveDto.getCustomerCode());
                if (virtualAccount != null && !virtualAccount.isEmpty()) {
                    try {
                        virtualAccountCode = Integer.valueOf(virtualAccount);
                    } catch (NumberFormatException e) {
                        log.warn("가상계좌 코드 변환 실패: {}", virtualAccount);
                    }
                }
                
                customerUser = CustomerUser.builder()
                        .customerCode(saveDto.getCustomerCode())
                        .virtualAccountCode(virtualAccountCode)
                        .customerUserId(saveDto.getCustomerUserId())
                        .customerUserPw(passwordEncoder.encode(saveDto.getCustomerUserPw())) // 비밀번호 암호화
                        .customerUserName(saveDto.getCustomerUserName())
                        .contactNum(saveDto.getContactNum())
                        .email(saveDto.getEmail())
                        .endYn(saveDto.getEndYn() != null ? saveDto.getEndYn() : 0) // 기본값 0
                        .description("거래처사용자등록")
                        .build();
            }
            
            customerUser = customerUserRepository.save(customerUser);
            
            String action = isUpdate ? "수정" : "등록";
            log.info("거래처사용자 {} 완료 - 사용자코드: {}", action, customerUser.getCustomerUserCode());
            return RespDto.success("거래처사용자가 " + action + "되었습니다.", customerUser.getCustomerUserCode().toString());
            
        } catch (Exception e) {
            log.error("거래처사용자 저장 중 오류 발생", e);
            return RespDto.fail("거래처사용자 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처사용자 삭제 (하드 딜리트)
     */
    @Transactional
    public RespDto<String> deleteCustomerUser(Integer customerUserCode) {
        try {
            log.info("거래처사용자 삭제 시작 - 사용자코드: {}", customerUserCode);
            
            CustomerUser customerUser = customerUserRepository.findByCustomerUserCode(customerUserCode);
            if (customerUser == null) {
                return RespDto.fail("해당 거래처사용자를 찾을 수 없습니다.");
            }
            
            customerUserRepository.delete(customerUser);
            
            log.info("거래처사용자 삭제 완료 - 사용자코드: {}", customerUserCode);
            return RespDto.success("거래처사용자가 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("거래처사용자 삭제 중 오류 발생", e);
            return RespDto.fail("거래처사용자 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}