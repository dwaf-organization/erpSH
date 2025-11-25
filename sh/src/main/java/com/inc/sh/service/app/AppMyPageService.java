package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.mypage.respDto.AppMyPageInfoRespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.CustomerUser;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.CustomerUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppMyPageService {
    
    private final CustomerRepository customerRepository;
    private final CustomerUserRepository customerUserRepository;
    
    /**
     * [앱] 내정보조회
     */
    @Transactional(readOnly = true)
    public RespDto<AppMyPageInfoRespDto> getMyPageInfo(Integer customerCode, Integer customerUserCode) {
        try {
            log.info("[앱] 내정보조회 시작 - customerCode: {}, customerUserCode: {}", customerCode, customerUserCode);
            
            // 1. 거래처 정보 조회
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            if (customer == null) {
                log.warn("[앱] 존재하지 않는 거래처 - customerCode: {}", customerCode);
                return RespDto.fail("존재하지 않는 거래처입니다");
            }
            
            // 2. 사용자 정보 조회
            CustomerUser customerUser = customerUserRepository.findByCustomerUserCode(customerUserCode);
            if (customerUser == null) {
                log.warn("[앱] 존재하지 않는 사용자 - customerUserCode: {}", customerUserCode);
                return RespDto.fail("존재하지 않는 사용자입니다");
            }
            
            // 3. 권한 확인 (사용자가 해당 거래처 소속인지 확인)
            if (!customerUser.getCustomerCode().equals(customerCode)) {
                log.warn("[앱] 권한 없음 - customerUserCode: {}, customerCode: {} (실제: {})", 
                        customerUserCode, customerCode, customerUser.getCustomerCode());
                return RespDto.fail("해당 거래처의 사용자가 아닙니다");
            }
            
            // 4. 입금계좌 정보 조합 (은행명 + 계좌번호 + 예금주)
            String accountInfo = buildAccountInfo(
                    customer.getBankName(), 
                    customer.getAccountNum(), 
                    customer.getAccountHolder()
            );
            
            // 5. 응답 DTO 생성
            AppMyPageInfoRespDto responseDto = AppMyPageInfoRespDto.builder()
                    // 거래처 정보
                    .customerName(customer.getCustomerName())
                    .ownerName(customer.getOwnerName())
                    .bizNum(customer.getBizNum())
                    .bizType(customer.getBizType())
                    .bizSector(customer.getBizSector())
                    .accountInfo(accountInfo)
                    .mobileNum(customer.getMobileNum())
                    .zipCode(customer.getZipCode())
                    .addr(customer.getAddr())
                    .email(customer.getEmail())
                    .telNum(customer.getTelNum())
                    // 사용자 정보
                    .customerUserId(customerUser.getCustomerUserId())
                    .build();
            
            log.info("[앱] 내정보조회 완료 - customerCode: {}, customerName: {}, customerUserId: {}", 
                    customerCode, customer.getCustomerName(), customerUser.getCustomerUserId());
            
            return RespDto.success("내정보 조회 성공", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 내정보조회 실패 - customerCode: {}, customerUserCode: {}", customerCode, customerUserCode, e);
            return RespDto.fail("내정보 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 입금계좌 정보 조합 (은행명 + 계좌번호 + 예금주)
     */
    private String buildAccountInfo(String bankName, String accountNum, String accountHolder) {
        if (bankName == null && accountNum == null && accountHolder == null) {
            return null;
        }
        
        StringBuilder accountInfo = new StringBuilder();
        
        if (bankName != null && !bankName.trim().isEmpty()) {
            accountInfo.append(bankName.trim());
        }
        
        if (accountNum != null && !accountNum.trim().isEmpty()) {
            if (accountInfo.length() > 0) {
                accountInfo.append(" ");
            }
            accountInfo.append(accountNum.trim());
        }
        
        if (accountHolder != null && !accountHolder.trim().isEmpty()) {
            if (accountInfo.length() > 0) {
                accountInfo.append(" ");
            }
            accountInfo.append(accountHolder.trim());
        }
        
        return accountInfo.length() > 0 ? accountInfo.toString() : null;
    }
}