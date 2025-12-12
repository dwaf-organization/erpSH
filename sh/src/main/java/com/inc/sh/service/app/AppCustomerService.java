package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.respDto.AppAccountInfoRespDto;
import com.inc.sh.dto.order.respDto.AppOrderItemListRespDto;
import com.inc.sh.dto.app.respDto.AppMainRespDto;
import com.inc.sh.dto.customerUser.reqDto.AppLoginReqDto;
import com.inc.sh.dto.customerUser.respDto.AppLoginRespDto;
import com.inc.sh.dto.headquarter.respDto.AppHqVerifyRespDto;
import com.inc.sh.entity.Customer;
import com.inc.sh.entity.CustomerUser;
import com.inc.sh.entity.Headquarter;
import com.inc.sh.repository.CustomerRepository;
import com.inc.sh.repository.CustomerUserRepository;
import com.inc.sh.repository.CustomerWishlistRepository;
import com.inc.sh.repository.HeadquarterRepository;
import com.inc.sh.repository.ItemRepository;
import com.inc.sh.repository.OrderRepository;
import com.inc.sh.repository.ReturnRepository;
import com.inc.sh.repository.DepositsRepository;
import com.inc.sh.repository.VirtualAccountRepository;
import com.inc.sh.service.AccessLogService;
import com.inc.sh.util.HqAccessCodeGenerator;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * [앱전용] 본사 및 고객 관리 서비스
 * - 본사접속코드 검증
 * - 고객 로그인 처리
 * - 앱에 필요한 간단한 정보만 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppCustomerService {
    
    private final HeadquarterRepository headquarterRepository;
    private final CustomerUserRepository customerUserRepository;
    private final CustomerRepository customerRepository;
    private final CustomerWishlistRepository customerWishlistRepository;
    private final ItemRepository itemRepository;
    private final OrderRepository orderRepository;
    private final ReturnRepository returnRepository;
    private final DepositsRepository depositsRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // AccessLogService 의존성 추가 필요
    private final AccessLogService accessLogService;
    
    /**
     * [앱전용] 본사접속코드 검증
     * @param hqAccessCode 본사접속코드
     * @return 본사 기본 정보 (hqCode, companyName, inquiryTelNum)
     */
    @Transactional(readOnly = true)
    public RespDto<AppHqVerifyRespDto> verifyHqAccessCode(String hqAccessCode) {
        try {
            // 입력값 정리
            hqAccessCode = hqAccessCode != null ? hqAccessCode.trim() : null;
            
            log.info("[앱] 본사접속코드 검증 시작 - hqAccessCode: '{}', 길이: {}", hqAccessCode, hqAccessCode != null ? hqAccessCode.length() : "null");
            
            // 접속코드 형식 검증
            boolean isValid = HqAccessCodeGenerator.isValidHqAccessCode(hqAccessCode);
            log.info("[앱] 형식 검증 결과: {}", isValid);
            
            if (!isValid) {
                log.warn("[앱] 잘못된 본사접속코드 형식 - hqAccessCode: '{}'", hqAccessCode);
                return RespDto.fail("잘못된 접속코드 형식입니다.");
            }
            
            // 본사 조회
            Headquarter headquarter = headquarterRepository.findByHqAccessCode(hqAccessCode);
            if (headquarter == null) {
                log.warn("[앱] 존재하지 않는 본사접속코드 - hqAccessCode: '{}'", hqAccessCode);
                return RespDto.fail("존재하지 않는 접속코드입니다.");
            }
            
            // 응답 생성 (앱에 필요한 최소 정보만)
            AppHqVerifyRespDto responseDto = AppHqVerifyRespDto.from(headquarter);
            
            log.info("[앱] 본사접속코드 검증 완료 - hqCode: {}, companyName: {}", 
                    headquarter.getHqCode(), headquarter.getCompanyName());
            
            return RespDto.success("본사 정보 확인 완료", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 본사접속코드 검증 중 오류 발생 - hqAccessCode: '{}'", hqAccessCode, e);
            return RespDto.fail("접속코드 검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * [앱전용] 고객 사용자 로그인
     * @param request 로그인 요청 정보
     * @return 로그인 결과 및 사용자 정보
     */
    @Transactional(readOnly = true)
    public RespDto<AppLoginRespDto> login(AppLoginReqDto request) {
        try {
            log.info("[앱] 로그인 시도 - customerUserId: {}", request.getCustomerUserId());
            
            // 사용자 조회
            CustomerUser customerUser = customerUserRepository.findByCustomerUserId(request.getCustomerUserId());
            
            if (customerUser == null) {
                log.warn("[앱] 존재하지 않는 아이디 - customerUserId: {}", request.getCustomerUserId());
                return RespDto.fail("아이디가 틀림");
            }
            
            // 퇴사 여부 확인
            if (customerUser.getEndYn() != null && customerUser.getEndYn() == 1) {
                log.warn("[앱] 퇴사한 사용자 로그인 시도 - customerUserId: {}", request.getCustomerUserId());
                return RespDto.fail("퇴사로 인한 로그인 불가");
            }
            
            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getCustomerUserPw(), customerUser.getCustomerUserPw())) {
                log.warn("[앱] 비밀번호 불일치 - customerUserId: {}", request.getCustomerUserId());
                return RespDto.fail("비밀번호틀림");
            }
            
            // 고객 정보 조회 (가상계좌코드 가져오기)
            Customer customer = customerRepository.findByCustomerCode(customerUser.getCustomerCode());
            
            // 응답 생성
            AppLoginRespDto responseDto = AppLoginRespDto.builder()
                    .customerUserCode(customerUser.getCustomerUserCode())
                    .customerCode(customerUser.getCustomerCode())
                    .customerUserName(customerUser.getCustomerUserName())
                    .virtualAccountCode(customer != null ? customer.getVirtualAccountCode() : null)
                    .build();
            
            log.info("[앱] 로그인 성공 - customerUserCode: {}, customerCode: {}", 
                    customerUser.getCustomerUserCode(), customerUser.getCustomerCode());
            
            return RespDto.success("로그인 성공", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 로그인 중 오류 발생 - customerUserId: {}", request.getCustomerUserId(), e);
            return RespDto.fail("로그인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * [앱전용] 메인페이지 정보 조회
     * @param customerCode 거래처 코드
     * @return 메인페이지 정보
     */
    @Transactional(readOnly = true)
    public RespDto<AppMainRespDto> getMainInfo(Integer customerCode) {
        try {
            log.info("[앱] 메인페이지 정보 조회 시작 - customerCode: {}", customerCode);
            
            // 거래처 정보 조회
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            if (customer == null) {
                log.warn("[앱] 존재하지 않는 거래처 - customerCode: {}", customerCode);
                return RespDto.fail("존재하지 않는 거래처입니다.");
            }
            
            // 최근 주문 정보 조회 (order_dt 기준 최근 1건)
            Object[] recentOrder = orderRepository.findRecentOrderByCustomerCode(customerCode);
            String recentOrderNo = null;
            Integer recentOrderAmt = null;
            if (recentOrder != null && recentOrder.length >= 2) {
                recentOrderNo = (String) recentOrder[0];
                recentOrderAmt = (Integer) recentOrder[1];
            }
            
            // 최근 반품 요청 금액 조회 (return_request_dt 기준 최근 1건)
            Integer recentReturnAmt = returnRepository.findRecentReturnAmountByCustomerCode(customerCode);
            
            // 최근 입금 금액 조회 (deposit_date 기준 최근 1건)
            Integer recentDepositAmt = depositsRepository.findRecentDepositAmountByCustomerCode(customerCode);
            
            // 응답 생성
            AppMainRespDto responseDto = AppMainRespDto.builder()
                    .customerName(customer.getCustomerName())
                    .ownerName(customer.getOwnerName())
                    .balanceAmt(customer.getBalanceAmt())
                    .recentOrderNo(recentOrderNo)
                    .recentOrderAmt(recentOrderAmt)
                    .recentReturnAmt(recentReturnAmt)
                    .recentDepositAmt(recentDepositAmt)
                    .build();
            
            log.info("[앱] 메인페이지 정보 조회 완료 - customerCode: {}, customerName: {}", 
                    customerCode, customer.getCustomerName());
            
            return RespDto.success("메인페이지 정보 조회 완료", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 메인페이지 정보 조회 중 오류 발생 - customerCode: {}", customerCode, e);
            return RespDto.fail("메인페이지 정보 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * [앱전용] 가상계좌 정보 조회
     * @param customerCode 거래처 코드
     * @return 가상계좌 정보, 잔액, 여신한도
     */
    @Transactional(readOnly = true)
    public RespDto<AppAccountInfoRespDto> getAccountInfo(Integer customerCode) {
        try {
            log.info("[앱] 가상계좌 정보 조회 시작 - customerCode: {}", customerCode);
            
            // 거래처 정보 조회 (잔액, 여신한도 확인)
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            if (customer == null) {
                log.warn("[앱] 존재하지 않는 거래처 - customerCode: {}", customerCode);
                return RespDto.fail("존재하지 않는 거래처입니다.");
            }
            
            // 가상계좌 정보 조회 (linked_customer_code로 JOIN)
            List<Object[]> virtualAccountList = virtualAccountRepository.findVirtualAccountByCustomerCode(customerCode);
            
            Object[] virtualAccountInfo = null;
            if (virtualAccountList != null && !virtualAccountList.isEmpty()) {
                virtualAccountInfo = virtualAccountList.get(0);
            }
            
            String virtualAccountNum = null;
            String bankName = null;
            String virtualAccountStatus = null;
            
            if (virtualAccountInfo != null && virtualAccountInfo.length >= 3) {
                virtualAccountNum = String.valueOf(virtualAccountInfo[0]);
                bankName = String.valueOf(virtualAccountInfo[1]);
                virtualAccountStatus = String.valueOf(virtualAccountInfo[2]);
            }
            
            // 응답 생성 (여신한도 추가)
            AppAccountInfoRespDto responseDto = AppAccountInfoRespDto.builder()
                    .virtualAccountNum(virtualAccountNum)
                    .bankName(bankName)
                    .virtualAccountStatus(virtualAccountStatus)
                    .balanceAmt(customer.getBalanceAmt())
                    .creditLimit(customer.getCreditLimit())  // 여신한도 추가
                    .build();
            
            log.info("[앱] 가상계좌 정보 조회 완료 - customerCode: {}, virtualAccountNum: {}, creditLimit: {}", 
                    customerCode, virtualAccountNum, customer.getCreditLimit());
            
            return RespDto.success("가상계좌 정보 조회 완료", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 가상계좌 정보 조회 중 오류 발생 - customerCode: {}", customerCode, e);
            return RespDto.fail("가상계좌 정보 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * [앱전용] 고객 사용자 로그인 (로그 기능 추가)
     */
    @Transactional(readOnly = true)
    public RespDto<AppLoginRespDto> login(AppLoginReqDto request, HttpServletRequest httpRequest) {
        try {
            log.info("[앱] 로그인 시도 - customerUserId: {}", request.getCustomerUserId());
            
            // 사용자 조회
            CustomerUser customerUser = customerUserRepository.findByCustomerUserId(request.getCustomerUserId());
            
            if (customerUser == null) {
                log.warn("[앱] 존재하지 않는 아이디 - customerUserId: {}", request.getCustomerUserId());
                
                // 실패 로그 기록 (hqCode는 고객 정보에서 가져올 수 없으므로 null 처리 또는 별도 로직)
                accessLogService.logFailureAccess("CUSTOMER", request.getCustomerUserId(), 
                        null, "사용자정보 없음", httpRequest);
                return RespDto.fail("아이디가 틀림");
            }
            
            // 퇴사 여부 확인
            if (customerUser.getEndYn() != null && customerUser.getEndYn() == 1) {
                log.warn("[앱] 퇴사한 사용자 로그인 시도 - customerUserId: {}", request.getCustomerUserId());
                
                // 실패 로그 기록
                accessLogService.logFailureAccess("CUSTOMER", String.valueOf(customerUser.getCustomerUserCode()), 
                        getHqCodeByCustomerCode(customerUser.getCustomerCode()), "퇴사한 사용자", httpRequest);
                return RespDto.fail("퇴사로 인한 로그인 불가");
            }
            
            // 비밀번호 검증
            if (!passwordEncoder.matches(request.getCustomerUserPw(), customerUser.getCustomerUserPw())) {
                log.warn("[앱] 비밀번호 불일치 - customerUserId: {}", request.getCustomerUserId());
                
                // 실패 로그 기록
                accessLogService.logFailureAccess("CUSTOMER", String.valueOf(customerUser.getCustomerUserCode()), 
                        getHqCodeByCustomerCode(customerUser.getCustomerCode()), "비밀번호 불일치", httpRequest);
                return RespDto.fail("비밀번호틀림");
            }
            
            // 로그인 성공 로그 기록
            accessLogService.logSuccessAccess("CUSTOMER", String.valueOf(customerUser.getCustomerUserCode()), 
                    getHqCodeByCustomerCode(customerUser.getCustomerCode()), httpRequest);
            
            // 고객 정보 조회 (가상계좌코드 가져오기)
            Customer customer = customerRepository.findByCustomerCode(customerUser.getCustomerCode());
            
            // 응답 생성
            AppLoginRespDto responseDto = AppLoginRespDto.builder()
                    .customerUserCode(customerUser.getCustomerUserCode())
                    .customerCode(customerUser.getCustomerCode())
                    .customerUserName(customerUser.getCustomerUserName())
                    .virtualAccountCode(customer != null ? customer.getVirtualAccountCode() : null)
                    .build();
            
            log.info("[앱] 로그인 성공 - customerUserCode: {}, customerCode: {}", 
                    customerUser.getCustomerUserCode(), customerUser.getCustomerCode());
            
            return RespDto.success("로그인 성공", responseDto);
            
        } catch (Exception e) {
            log.error("[앱] 로그인 중 오류 발생 - customerUserId: {}", request.getCustomerUserId(), e);
            return RespDto.fail("로그인 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처코드로 본사코드 조회 (로그용)
     */
    private Integer getHqCodeByCustomerCode(Integer customerCode) {
        try {
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            return customer != null ? customer.getHqCode() : null;
        } catch (Exception e) {
            log.warn("본사코드 조회 실패 - customerCode: {}", customerCode);
            return null;
        }
    }
}