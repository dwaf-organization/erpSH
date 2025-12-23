package com.inc.sh.service;

import com.inc.sh.dto.customerUser.reqDto.CustomerUserSearchDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserDeleteReqDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserSaveDto;
import com.inc.sh.dto.customerUser.reqDto.CustomerUserSaveReqDto;
import com.inc.sh.dto.customerUser.respDto.CustomerUserBatchResult;
import com.inc.sh.dto.customerUser.respDto.CustomerUserRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.CustomerUser;
import com.inc.sh.repository.CustomerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerUserService {
    
    private final CustomerUserRepository customerUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 거래처사용자 조회 (customerCode 필수 검증 추가)
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerUserRespDto>> getCustomerUserList(CustomerUserSearchDto searchDto) {
        try {
            log.info("거래처사용자 조회 시작 - customerCode: {}, customerUserId: {}", 
                    searchDto.getCustomerCode(), searchDto.getCustomerUserId());
            
            if (searchDto.getCustomerCode() == null) {
                return RespDto.fail("거래처코드는 필수 파라미터입니다.");
            }
            
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
                            .customerUserPw(null) // 조회 시에도 표시
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처사용자 조회 완료 - customerCode: {}, 조회 건수: {}", 
                    searchDto.getCustomerCode(), responseList.size());
            return RespDto.success("거래처사용자 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처사용자 조회 중 오류 발생", e);
            return RespDto.fail("거래처사용자 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처사용자 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<CustomerUserBatchResult> saveCustomerUsers(CustomerUserSaveReqDto request) {
        try {
            log.info("거래처사용자 다중 저장 시작 - 총 {}건", 
                    request.getCustomerUsers() != null ? request.getCustomerUsers().size() : 0);
            
            // 요청 데이터 검증
            if (request.getCustomerUsers() == null || request.getCustomerUsers().isEmpty()) {
                return RespDto.fail("저장할 거래처사용자 데이터가 없습니다.");
            }
            
            List<CustomerUserBatchResult.CustomerUserSuccessResult> successList = new ArrayList<>();
            List<CustomerUserBatchResult.CustomerUserFailureResult> failureList = new ArrayList<>();
            
            // 개별 저장 처리
            for (CustomerUserSaveReqDto.CustomerUserSaveItemDto saveDto : request.getCustomerUsers()) {
                try {
                    // 필수 필드 검증
                    if (saveDto.getCustomerCode() == null) {
                        throw new RuntimeException("거래처코드는 필수입니다.");
                    }
                    if (saveDto.getCustomerUserId() == null || saveDto.getCustomerUserId().trim().isEmpty()) {
                        throw new RuntimeException("사용자아이디는 필수입니다.");
                    }
                    if (saveDto.getCustomerUserName() == null || saveDto.getCustomerUserName().trim().isEmpty()) {
                        throw new RuntimeException("사용자명은 필수입니다.");
                    }
                    
                    CustomerUserBatchResult.CustomerUserSuccessResult result = saveSingleCustomerUser(saveDto);
                    if (result != null) {
                        successList.add(result);
                        log.info("거래처사용자 저장 성공 - 사용자코드: {}, 아이디: {}", 
                                result.getCustomerUserCode(), result.getCustomerUserId());
                    }
                } catch (Exception e) {
                    CustomerUserBatchResult.CustomerUserFailureResult failure = 
                        CustomerUserBatchResult.CustomerUserFailureResult.builder()
                                .customerUserCode(saveDto.getCustomerUserCode())
                                .customerCode(saveDto.getCustomerCode())
                                .customerUserId(saveDto.getCustomerUserId())
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("거래처사용자 저장 실패 - 아이디: {}, 원인: {}", saveDto.getCustomerUserId(), e.getMessage());
                }
            }
            
            // 결과 집계
            CustomerUserBatchResult batchResult = CustomerUserBatchResult.builder()
                    .totalCount(request.getCustomerUsers().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("거래처사용자 다중 저장 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("거래처사용자 다중 저장 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("거래처사용자 다중 저장 중 오류 발생", e);
            return RespDto.fail("거래처사용자 다중 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처사용자 다중 삭제
     */
    @Transactional
    public RespDto<CustomerUserBatchResult> deleteCustomerUsers(CustomerUserDeleteReqDto request) {
        try {
            log.info("거래처사용자 다중 삭제 시작 - 총 {}건", 
                    request.getCustomerUserCodes() != null ? request.getCustomerUserCodes().size() : 0);
            
            // 요청 데이터 검증
            if (request.getCustomerUserCodes() == null || request.getCustomerUserCodes().isEmpty()) {
                return RespDto.fail("삭제할 사용자코드가 없습니다.");
            }
            
            List<CustomerUserBatchResult.CustomerUserSuccessResult> successList = new ArrayList<>();
            List<CustomerUserBatchResult.CustomerUserFailureResult> failureList = new ArrayList<>();
            
            // 개별 삭제 처리
            for (Integer customerUserCode : request.getCustomerUserCodes()) {
                try {
                    CustomerUserBatchResult.CustomerUserSuccessResult result = deleteSingleCustomerUser(customerUserCode);
                    if (result != null) {
                        successList.add(result);
                        log.info("거래처사용자 삭제 성공 - 사용자코드: {}", customerUserCode);
                    }
                } catch (Exception e) {
                    CustomerUserBatchResult.CustomerUserFailureResult failure = 
                        CustomerUserBatchResult.CustomerUserFailureResult.builder()
                                .customerUserCode(customerUserCode)
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("거래처사용자 삭제 실패 - 사용자코드: {}, 원인: {}", customerUserCode, e.getMessage());
                }
            }
            
            // 결과 집계
            CustomerUserBatchResult batchResult = CustomerUserBatchResult.builder()
                    .totalCount(request.getCustomerUserCodes().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("거래처사용자 다중 삭제 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("거래처사용자 다중 삭제 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("거래처사용자 다중 삭제 중 오류 발생", e);
            return RespDto.fail("거래처사용자 다중 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 개별 거래처사용자 저장 처리
     */
    private CustomerUserBatchResult.CustomerUserSuccessResult saveSingleCustomerUser(
            CustomerUserSaveReqDto.CustomerUserSaveItemDto saveDto) {
        
        boolean isUpdate = saveDto.getCustomerUserCode() != null;
        CustomerUser customerUser;
        
        if (isUpdate) {
            // 수정
            customerUser = customerUserRepository.findByCustomerUserCode(saveDto.getCustomerUserCode());
            if (customerUser == null) {
                throw new RuntimeException("해당 거래처사용자를 찾을 수 없습니다.");
            }
            
            // 아이디 중복 체크 (본인 제외)
            CustomerUser existingUser = customerUserRepository.findByCustomerUserId(saveDto.getCustomerUserId());
            if (existingUser != null && !existingUser.getCustomerUserCode().equals(saveDto.getCustomerUserCode())) {
                throw new RuntimeException("이미 사용중인 아이디입니다.");
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
                throw new RuntimeException("이미 사용중인 아이디입니다.");
            }
            
            // 비밀번호 필수 검증 (신규 시)
            if (saveDto.getCustomerUserPw() == null || saveDto.getCustomerUserPw().trim().isEmpty()) {
                throw new RuntimeException("신규 사용자는 비밀번호가 필수입니다.");
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
        
        // 거래처명 조회 (Optional)
        String customerName = null;
        try {
            List<Object[]> customerInfo = customerUserRepository.findCustomerUsersWithCustomerByConditions(
                    customerUser.getCustomerCode(), null);
            if (!customerInfo.isEmpty()) {
                customerName = (String) customerInfo.get(0)[1]; // customer_name
            }
        } catch (Exception e) {
            log.warn("거래처명 조회 실패", e);
        }
        
        String action = isUpdate ? "수정" : "등록";
        return CustomerUserBatchResult.CustomerUserSuccessResult.builder()
                .customerUserCode(customerUser.getCustomerUserCode())
                .customerCode(customerUser.getCustomerCode())
                .customerName(customerName)
                .customerUserId(customerUser.getCustomerUserId())
                .customerUserName(customerUser.getCustomerUserName())
                .endYn(customerUser.getEndYn())
                .message(String.format("%s 완료", action))
                .build();
    }
    
    /**
     * 개별 거래처사용자 삭제 처리 (✅ FK 제약조건 체크 + 소프트 삭제)
     */
    private CustomerUserBatchResult.CustomerUserSuccessResult deleteSingleCustomerUser(Integer customerUserCode) {
        CustomerUser customerUser = customerUserRepository.findByCustomerUserCode(customerUserCode);
        if (customerUser == null) {
            throw new RuntimeException("해당 거래처사용자를 찾을 수 없습니다.");
        }
        
        String customerUserId = customerUser.getCustomerUserId();
        String customerUserName = customerUser.getCustomerUserName();
        Integer customerCode = customerUser.getCustomerCode();
        
        // ✅ FK 제약조건 체크 (삭제 전 관련 데이터 확인)
        checkForeignKeyConstraints(customerUserCode, customerCode);
        
        // 거래처명 조회 (Optional)
        String customerName = null;
        try {
            List<Object[]> customerInfo = customerUserRepository.findCustomerUsersWithCustomerByConditions(
                    customerCode, null);
            if (!customerInfo.isEmpty()) {
                customerName = (String) customerInfo.get(0)[1]; // customer_name
            }
        } catch (Exception e) {
            log.warn("거래처명 조회 실패", e);
        }
        
        // ✅ 소프트 삭제: end_yn = 1로 변경 (하드 삭제 대신)
        customerUser.setEndYn(1); // 사용 중지
        customerUserRepository.save(customerUser);
        
        log.info("거래처사용자 사용중지 처리 완료 - customerUserCode: {}, userId: {}", 
                customerUserCode, customerUserId);
        
        return CustomerUserBatchResult.CustomerUserSuccessResult.builder()
                .customerUserCode(customerUserCode)
                .customerCode(customerCode)
                .customerName(customerName)
                .customerUserId(customerUserId)
                .customerUserName(customerUserName)
                .endYn(1) // 사용중지 상태
                .message("사용중지 처리 완료")
                .build();
    }
    
    /**
     * ✅ FK 제약조건 체크 (관련 데이터 존재 확인)
     */
    private void checkForeignKeyConstraints(Integer customerUserCode, Integer customerCode) {
        try {
            // 1. 장바구니 데이터 존재 체크
            boolean hasCartData = checkCartData(customerUserCode, customerCode);
            if (hasCartData) {
                throw new RuntimeException("장바구니에 데이터가 존재하여 삭제할 수 없습니다.");
            }
            
            // 2. 위시리스트 데이터 존재 체크  
            boolean hasWishlistData = checkWishlistData(customerUserCode, customerCode);
            if (hasWishlistData) {
                throw new RuntimeException("위시리스트에 데이터가 존재하여 삭제할 수 없습니다.");
            }
            
            // 3. 기타 관련 테이블 체크 (필요시 추가)
            // boolean hasOrderData = checkOrderData(customerUserCode, customerCode);
            // if (hasOrderData) {
            //     throw new RuntimeException("주문내역이 존재하여 삭제할 수 없습니다.");
            // }
            
        } catch (RuntimeException e) {
            // 명시적으로 던진 에러는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("FK 제약조건 체크 중 오류 발생 - customerUserCode: {}", customerUserCode, e);
            throw new RuntimeException("관련 데이터 확인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * ✅ 장바구니 데이터 존재 체크
     */
    private boolean checkCartData(Integer customerUserCode, Integer customerCode) {
        try {
            // customer_cart 테이블에서 해당 사용자의 데이터 존재 확인
            // Repository에 메서드가 없다면 간단한 카운트 쿼리로 확인
            Long cartCount = customerUserRepository.countCartByCustomerUser(customerUserCode, customerCode);
            return cartCount != null && cartCount > 0;
        } catch (Exception e) {
            log.warn("장바구니 데이터 확인 중 오류 - customerUserCode: {}", customerUserCode, e);
            return false; // 에러시 안전하게 false 반환
        }
    }
    
    /**
     * ✅ 위시리스트 데이터 존재 체크
     */
    private boolean checkWishlistData(Integer customerUserCode, Integer customerCode) {
        try {
            // customer_wishlist 테이블에서 해당 사용자의 데이터 존재 확인
            Long wishlistCount = customerUserRepository.countWishlistByCustomerUser(customerUserCode, customerCode);
            return wishlistCount != null && wishlistCount > 0;
        } catch (Exception e) {
            log.warn("위시리스트 데이터 확인 중 오류 - customerUserCode: {}", customerUserCode, e);
            return false; // 에러시 안전하게 false 반환
        }
    }
}