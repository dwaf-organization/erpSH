package com.inc.sh.service;

import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSearchDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSaveDto;
import com.inc.sh.dto.customerDeposit.respDto.CustomerDepositRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.*;
import com.inc.sh.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerDepositService {
    
    private final DepositsRepository depositsRepository;
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * 거래처수금처리 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerDepositRespDto>> getCustomerDepositList(CustomerDepositSearchDto searchDto) {
        try {
            log.info("거래처수금처리 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = depositsRepository.findCustomerDepositsWithConditions(
                    searchDto.getCustomerCode(),
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getDepositTypeCode()
            );
            
            List<CustomerDepositRespDto> responseList = results.stream()
                    .map(result -> CustomerDepositRespDto.builder()
                            .depositId((Integer) result[0])
                            .customerCode((Integer) result[1])
                            .customerName((String) result[2])
                            .depositDate(formatDepositDate((String) result[3]))
                            .depositAmount(((Number) result[4]).intValue())
                            .depositMethod((String) result[5])
                            .depositorName((String) result[6])
                            .note((String) result[7])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처수금처리 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처수금처리 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처수금처리 조회 중 오류 발생", e);
            return RespDto.fail("거래처수금처리 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처수금처리 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveCustomerDeposit(CustomerDepositSaveDto saveDto) {
        try {
            log.info("거래처수금처리 저장 시작 - 입금코드: {}, 거래처코드: {}, 입금금액: {}", 
                    saveDto.getDepositId(), saveDto.getCustomerCode(), saveDto.getDepositAmount());
            
            // 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            boolean isUpdate = saveDto.getDepositId() != null;
            Deposits originalDeposit = null;
            int originalAmount = 0;
            int originalBalanceAmt = customer.getBalanceAmt(); // 수정 전 잔액
            
            // 수정인 경우 기존 입금 정보 조회
            if (isUpdate) {
                originalDeposit = depositsRepository.findById(saveDto.getDepositId()).orElse(null);
                if (originalDeposit == null) {
                    return RespDto.fail("해당 입금내역을 찾을 수 없습니다.");
                }
                originalAmount = originalDeposit.getDepositAmount();
                log.info("기존 입금 정보 - 기존금액: {}, 새금액: {}, 거래처 현재잔액: {}", 
                        originalAmount, saveDto.getDepositAmount(), originalBalanceAmt);
            }
            
            // 가상계좌코드 설정 (충전형=1일 때만 설정)
            Integer virtualAccountCode = null;
            if ("1".equals(saveDto.getDepositMethod())) { // 가상계좌
                // 충전형 거래처의 가상계좌코드 사용 (실제 구현에서는 거래처의 가상계좌 정보를 가져와야 함)
                virtualAccountCode = 1; // 임시값, 실제로는 customer 테이블에서 가져와야 함
            }
            
            // 입금 정보 저장
            Deposits deposit;
            if (isUpdate) {
                // 수정
                deposit = originalDeposit;
                deposit.setCustomerCode(saveDto.getCustomerCode());
                deposit.setVirtualAccountCode(virtualAccountCode);
                deposit.setDepositDate(saveDto.getDepositDate());
                deposit.setDepositAmount(saveDto.getDepositAmount());
                deposit.setDepositMethod(saveDto.getDepositMethod());
                deposit.setDepositorName(saveDto.getDepositorName());
                deposit.setNote(saveDto.getNote());
                deposit.setDescription("거래처입금수정");
            } else {
                // 신규
                deposit = Deposits.builder()
                        .customerCode(saveDto.getCustomerCode())
                        .virtualAccountCode(virtualAccountCode)
                        .depositDate(saveDto.getDepositDate())
                        .depositAmount(saveDto.getDepositAmount())
                        .depositMethod(saveDto.getDepositMethod())
                        .depositorName(saveDto.getDepositorName())
                        .note(saveDto.getNote())
                        .description("거래처입금등록")
                        .build();
            }
            
            deposit = depositsRepository.save(deposit);
            
            // 거래처 잔액 업데이트
            int newBalanceAmt;
            if (isUpdate) {
                // 수정 시: 기존 금액 차감 후 새 금액 추가
                int balanceDiff = saveDto.getDepositAmount() - originalAmount;
                newBalanceAmt = customer.getBalanceAmt() + balanceDiff;
                customer.setBalanceAmt(newBalanceAmt);
                log.info("잔액 수정 계산 - 기존잔액: {}, 금액차이: {}, 새잔액: {}", 
                        originalBalanceAmt, balanceDiff, newBalanceAmt);
            } else {
                // 신규 시: 입금금액 추가
                newBalanceAmt = customer.getBalanceAmt() + saveDto.getDepositAmount();
                customer.setBalanceAmt(newBalanceAmt);
                log.info("잔액 신규 계산 - 기존잔액: {}, 입금금액: {}, 새잔액: {}", 
                        originalBalanceAmt, saveDto.getDepositAmount(), newBalanceAmt);
            }
            customerRepository.save(customer);
            
            // 거래처계좌거래내역 처리
            if (isUpdate) {
                // 수정 시: 기존 거래내역 삭제 후 새로 생성
                customerAccountTransactionsRepository.deleteByReferenceIdAndReferenceType(
                        originalDeposit.getDepositId().toString(), "입금확인");
                log.info("기존 거래내역 삭제 완료 - 참조ID: {}", originalDeposit.getDepositId().toString());
            }
            
            // 새 거래내역 생성
            CustomerAccountTransactions transaction = CustomerAccountTransactions.builder()
                    .customerCode(saveDto.getCustomerCode())
                    .virtualAccountCode(virtualAccountCode)
                    .transactionDate(saveDto.getDepositDate())
                    .transactionType("입금")
                    .amount(saveDto.getDepositAmount())
                    .balanceAfter(newBalanceAmt)  // 수정된 최종 잔액 사용
                    .referenceType("입금확인")
                    .referenceId(deposit.getDepositId().toString())
                    .note(saveDto.getNote())
                    .description("거래처입금")
                    .build();
            
            customerAccountTransactionsRepository.save(transaction);
            
            String action = isUpdate ? "수정" : "등록";
            log.info("거래처수금처리 {} 완료 - 입금코드: {}, 최종잔액: {}", action, deposit.getDepositId(), newBalanceAmt);
            
            String resultMessage = String.format("거래처수금처리가 %s되었습니다. (잔액: %,d원)", action, newBalanceAmt);
            return RespDto.success(resultMessage, deposit.getDepositId().toString());
            
        } catch (Exception e) {
            log.error("거래처수금처리 저장 중 오류 발생", e);
            return RespDto.fail("거래처수금처리 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처수금처리 삭제
     */
    @Transactional
    public RespDto<String> deleteCustomerDeposit(Integer depositId) {
        try {
            log.info("거래처수금처리 삭제 시작 - 입금코드: {}", depositId);
            
            // 입금 정보 조회
            Deposits deposit = depositsRepository.findById(depositId).orElse(null);
            if (deposit == null) {
                return RespDto.fail("해당 입금내역을 찾을 수 없습니다.");
            }
            
            // 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(deposit.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            // 거래처 잔액 롤백 (입금금액만큼 차감)
            customer.setBalanceAmt(customer.getBalanceAmt() - deposit.getDepositAmount());
            customerRepository.save(customer);
            
            // 거래처계좌거래내역 삭제
            customerAccountTransactionsRepository.deleteByReferenceIdAndReferenceType(
                    depositId.toString(), "입금확인");
            
            // 입금 정보 삭제
            depositsRepository.delete(deposit);
            
            log.info("거래처수금처리 삭제 완료 - 입금코드: {}", depositId);
            return RespDto.success("거래처수금처리가 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("거래처수금처리 삭제 중 오류 발생", e);
            return RespDto.fail("거래처수금처리 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 입금일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatDepositDate(String depositDate) {
        try {
            if (depositDate != null && depositDate.length() == 8) {
                LocalDate date = LocalDate.parse(depositDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return depositDate;
        } catch (Exception e) {
            log.warn("입금일자 포맷 변환 실패: {}", depositDate);
            return depositDate;
        }
    }
}