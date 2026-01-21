package com.inc.sh.service;

import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSearchDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositDeleteReqDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSaveDto;
import com.inc.sh.dto.customerDeposit.respDto.CustomerDepositBatchResult;
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
import java.util.ArrayList;
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
     * 거래처수금처리 조회 (referenceId, balanceAfter 포함)
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerDepositRespDto>> getCustomerDepositList(CustomerDepositSearchDto searchDto) {
        try {
            log.info("거래처수금처리 조회 시작 - hqCode: {}, 거래처: {}, 기간: {}~{}, 입금유형: {}", 
                    searchDto.getHqCode(), searchDto.getCustomerCode(), searchDto.getStartDate(), 
                    searchDto.getEndDate(), searchDto.getDepositMethod());
            
            List<Object[]> results = depositsRepository.findCustomerDepositsWithConditionsAndBalanceWithHqCode(
                    searchDto.getCustomerCode(),
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getDepositMethod(),
                    searchDto.getHqCode()
            );
            
            List<CustomerDepositRespDto> responseList = results.stream()
                    .map(result -> CustomerDepositRespDto.builder()
                            .depositId((Integer) result[0])
                            .customerCode((Integer) result[1])
                            .customerName((String) result[2])
                            .depositDate(formatDepositDate((String) result[3]))
                            .depositAmount(((Number) result[4]).intValue())
                            .depositMethod((Integer) result[5])
                            .depositorName((String) result[6])
                            .note((String) result[7])
                            .referenceId((String) result[8])                    // 참조코드 (주문번호)
                            .balanceAfter(result[9] != null ? ((Number) result[9]).intValue() : null) // 거래 후 잔액
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처수금처리 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("거래처수금처리 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처수금처리 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("거래처수금처리 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처수금처리 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<CustomerDepositBatchResult> saveCustomerDeposits(CustomerDepositSaveDto reqDto) {
        
        log.info("거래처수금처리 다중 저장 시작 - 총 {}건", reqDto.getDeposits().size());
        
        List<CustomerDepositRespDto> successData = new ArrayList<>();
        List<CustomerDepositBatchResult.CustomerDepositErrorDto> failData = new ArrayList<>();
        
        for (CustomerDepositSaveDto.CustomerDepositItemDto deposit : reqDto.getDeposits()) {
            try {
                // 개별 거래처수금처리 저장 처리
                CustomerDepositRespDto savedDeposit = saveSingleCustomerDeposit(deposit);
                successData.add(savedDeposit);
                
                log.info("거래처수금처리 저장 성공 - depositId: {}, customerCode: {}, amount: {}", 
                        savedDeposit.getDepositId(), savedDeposit.getCustomerCode(), savedDeposit.getDepositAmount());
                
            } catch (Exception e) {
                log.error("거래처수금처리 저장 실패 - customerCode: {}, 에러: {}", deposit.getCustomerCode(), e.getMessage());
                
                // 에러 시 거래처명 조회 시도
                String customerName = getCustomerNameSafely(deposit.getCustomerCode());
                
                CustomerDepositBatchResult.CustomerDepositErrorDto errorDto = CustomerDepositBatchResult.CustomerDepositErrorDto.builder()
                        .depositId(deposit.getDepositId())
                        .customerCode(deposit.getCustomerCode())
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        CustomerDepositBatchResult result = CustomerDepositBatchResult.builder()
                .totalCount(reqDto.getDeposits().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("거래처수금처리 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("거래처수금처리 다중 저장 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getDeposits().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 거래처수금처리 다중 삭제
     */
    @Transactional
    public RespDto<CustomerDepositBatchResult> deleteCustomerDeposits(CustomerDepositDeleteReqDto reqDto) {
        
        log.info("거래처수금처리 다중 삭제 시작 - 총 {}건", reqDto.getDepositIds().size());
        
        List<CustomerDepositRespDto> successData = new ArrayList<>();
        List<CustomerDepositBatchResult.CustomerDepositErrorDto> failData = new ArrayList<>();
        
        for (Integer depositId : reqDto.getDepositIds()) {
            try {
                // 개별 거래처수금처리 삭제 처리
                deleteSingleCustomerDeposit(depositId);
                
                // 삭제 성공 시 간단한 응답 데이터 생성
                CustomerDepositRespDto deletedDeposit = CustomerDepositRespDto.builder()
                        .depositId(depositId)
                        .build();
                successData.add(deletedDeposit);
                
                log.info("거래처수금처리 삭제 성공 - depositId: {}", depositId);
                
            } catch (Exception e) {
                log.error("거래처수금처리 삭제 실패 - depositId: {}, 에러: {}", depositId, e.getMessage());
                
                // 에러 시 입금 정보 조회 시도
                String customerName = "알 수 없음";
                Integer customerCode = null;
                try {
                    Deposits deposit = depositsRepository.findById(depositId).orElse(null);
                    if (deposit != null) {
                        customerCode = deposit.getCustomerCode();
                        customerName = getCustomerNameSafely(deposit.getCustomerCode());
                    }
                } catch (Exception ignored) {}
                
                CustomerDepositBatchResult.CustomerDepositErrorDto errorDto = CustomerDepositBatchResult.CustomerDepositErrorDto.builder()
                        .depositId(depositId)
                        .customerCode(customerCode)
                        .customerName(customerName)
                        .errorMessage(e.getMessage())
                        .build();
                
                failData.add(errorDto);
            }
        }
        
        // 배치 결과 생성
        CustomerDepositBatchResult result = CustomerDepositBatchResult.builder()
                .totalCount(reqDto.getDepositIds().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("거래처수금처리 삭제 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("거래처수금처리 다중 삭제 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getDepositIds().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 거래처수금처리 저장 처리 (referenceId 포함)
     */
    private CustomerDepositRespDto saveSingleCustomerDeposit(CustomerDepositSaveDto.CustomerDepositItemDto saveDto) {
        
        log.info("거래처수금처리 저장 시작 - 입금코드: {}, 거래처코드: {}, 입금금액: {}, 참조코드: {}", 
                saveDto.getDepositId(), saveDto.getCustomerCode(), saveDto.getDepositAmount(), saveDto.getReferenceId());
        
        // 거래처 조회
        Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다: " + saveDto.getCustomerCode());
        }
        
        boolean isUpdate = saveDto.getDepositId() != null;
        Deposits originalDeposit = null;
        int originalAmount = 0;
        int originalBalanceAmt = customer.getBalanceAmt();
        
        // 수정인 경우 기존 입금 정보 조회
        if (isUpdate) {
            originalDeposit = depositsRepository.findById(saveDto.getDepositId()).orElse(null);
            if (originalDeposit == null) {
                throw new RuntimeException("해당 입금내역을 찾을 수 없습니다: " + saveDto.getDepositId());
            }
            originalAmount = originalDeposit.getDepositAmount();
            log.info("기존 입금 정보 - 기존금액: {}, 새금액: {}, 거래처 현재잔액: {}", 
                    originalAmount, saveDto.getDepositAmount(), originalBalanceAmt);
        }
        
        // 가상계좌코드 설정
        Integer virtualAccountCode = null;
        if ("1".equals(saveDto.getDepositMethod())) {
            virtualAccountCode = 1; // 임시값
        }
        
        // 입금 정보 저장
        Deposits deposit;
        if (isUpdate) {
            deposit = originalDeposit;
            deposit.setCustomerCode(saveDto.getCustomerCode());
            deposit.setVirtualAccountCode(virtualAccountCode);
            deposit.setDepositDate(saveDto.getDepositDate());
            deposit.setDepositAmount(saveDto.getDepositAmount());
            deposit.setDepositMethod(saveDto.getDepositMethod());
            deposit.setDepositorName(saveDto.getDepositorName());
            deposit.setNote(saveDto.getNote());
            deposit.setDescription(saveDto.getReferenceId()); // 참조코드를 description에 저장
        } else {
            deposit = Deposits.builder()
                    .customerCode(saveDto.getCustomerCode())
                    .virtualAccountCode(virtualAccountCode)
                    .depositDate(saveDto.getDepositDate())
                    .depositAmount(saveDto.getDepositAmount())
                    .depositMethod(saveDto.getDepositMethod())
                    .depositorName(saveDto.getDepositorName())
                    .note(saveDto.getNote())
                    .description(saveDto.getReferenceId()) // 참조코드를 description에 저장
                    .build();
        }
        
        deposit = depositsRepository.save(deposit);
        
        // 거래처 잔액 업데이트
        int newBalanceAmt;
        if (isUpdate) {
            int balanceDiff = saveDto.getDepositAmount() - originalAmount;
            newBalanceAmt = customer.getBalanceAmt() + balanceDiff;
            customer.setBalanceAmt(newBalanceAmt);
            log.info("잔액 수정 계산 - 기존잔액: {}, 금액차이: {}, 새잔액: {}", 
                    originalBalanceAmt, balanceDiff, newBalanceAmt);
        } else {
            newBalanceAmt = customer.getBalanceAmt() + saveDto.getDepositAmount();
            customer.setBalanceAmt(newBalanceAmt);
            log.info("잔액 신규 계산 - 기존잔액: {}, 입금금액: {}, 새잔액: {}", 
                    originalBalanceAmt, saveDto.getDepositAmount(), newBalanceAmt);
        }
        customerRepository.save(customer);
        
        // 거래처계좌거래내역 처리
        if (isUpdate) {
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
                .balanceAfter(newBalanceAmt)
                .referenceType("입금확인")
                .referenceId(deposit.getDepositId().toString())
                .note(saveDto.getNote())
                .description("거래처입금")
                .build();
        
        customerAccountTransactionsRepository.save(transaction);
        
        String action = isUpdate ? "수정" : "등록";
        log.info("거래처수금처리 {} 완료 - 입금코드: {}, 참조코드: {}, 최종잔액: {}", 
                action, deposit.getDepositId(), saveDto.getReferenceId(), newBalanceAmt);
        
        // 응답 DTO 생성
        return CustomerDepositRespDto.builder()
                .depositId(deposit.getDepositId())
                .customerCode(deposit.getCustomerCode())
                .customerName(customer.getCustomerName())
                .depositDate(formatDepositDate(deposit.getDepositDate()))
                .depositAmount(deposit.getDepositAmount())
                .depositMethod(deposit.getDepositMethod())
                .depositorName(deposit.getDepositorName())
                .note(deposit.getNote())
                .referenceId(deposit.getDescription())  // description에서 참조코드 반환
                .balanceAfter(newBalanceAmt)             // 거래 후 잔액 반환
                .build();
    }
    
    /**
     * 개별 거래처수금처리 삭제 처리 (기존 로직 포함)
     */
    private void deleteSingleCustomerDeposit(Integer depositId) {
        
        log.info("거래처수금처리 삭제 시작 - 입금코드: {}", depositId);
        
        // 입금 정보 조회
        Deposits deposit = depositsRepository.findById(depositId).orElse(null);
        if (deposit == null) {
            throw new RuntimeException("해당 입금내역을 찾을 수 없습니다: " + depositId);
        }
        
        // 거래처 조회
        Customer customer = customerRepository.findByCustomerCode(deposit.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다: " + deposit.getCustomerCode());
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
    }
    
    /**
     * 거래처명 안전 조회 (에러 발생시 사용)
     */
    private String getCustomerNameSafely(Integer customerCode) {
        try {
            Customer customer = customerRepository.findByCustomerCode(customerCode);
            return customer != null ? customer.getCustomerName() : "알 수 없음";
        } catch (Exception e) {
            return "조회 실패";
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