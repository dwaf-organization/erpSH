package com.inc.sh.service;

import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSearchDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSaveDto;
import com.inc.sh.dto.customerAdjustment.respDto.CustomerAdjustmentRespDto;
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
public class CustomerAdjustmentService {
    
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * 거래처조정처리 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerAdjustmentRespDto>> getCustomerAdjustmentList(CustomerAdjustmentSearchDto searchDto) {
        try {
            log.info("거래처조정처리 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = customerAccountTransactionsRepository.findCustomerAdjustmentsWithConditions(
                    searchDto.getCustomerCode(),
                    searchDto.getAdjustmentDate()
            );
            
            List<CustomerAdjustmentRespDto> responseList = results.stream()
                    .map(result -> CustomerAdjustmentRespDto.builder()
                            .transactionCode((Integer) result[0])
                            .customerCode((Integer) result[1])
                            .customerName((String) result[2])
                            .adjustmentDate(formatAdjustmentDate((String) result[3]))
                            .adjustmentAmount(((Number) result[4]).intValue())
                            .note((String) result[5])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("거래처조정처리 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처조정처리 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처조정처리 조회 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처조정처리 저장 (신규/수정)
     */
    @Transactional
    public RespDto<String> saveCustomerAdjustment(CustomerAdjustmentSaveDto saveDto) {
        try {
            log.info("거래처조정처리 저장 시작 - 거래내역코드: {}, 거래처코드: {}, 조정금액: {}", 
                    saveDto.getTransactionCode(), saveDto.getCustomerCode(), saveDto.getAdjustmentAmount());
            
            // 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            boolean isUpdate = saveDto.getTransactionCode() != null;
            CustomerAccountTransactions originalTransaction = null;
            int originalAmount = 0;
            int originalBalanceAmt = customer.getBalanceAmt(); // 수정 전 잔액
            
            // 수정인 경우 기존 조정 정보 조회
            if (isUpdate) {
                originalTransaction = customerAccountTransactionsRepository.findById(saveDto.getTransactionCode()).orElse(null);
                if (originalTransaction == null) {
                    return RespDto.fail("해당 조정내역을 찾을 수 없습니다.");
                }
                originalAmount = originalTransaction.getAmount();
                log.info("기존 조정 정보 - 기존금액: {}, 새금액: {}, 거래처 현재잔액: {}", 
                        originalAmount, saveDto.getAdjustmentAmount(), originalBalanceAmt);
            }
            
            // 거래처 잔액 업데이트
            int newBalanceAmt;
            if (isUpdate) {
                // 수정 시: 기존 조정금액 되돌리고 새 조정금액 적용
                // 1. 기존 조정 되돌리기: 기존 금액의 반대로 적용
                // 2. 새 조정 적용
                int rollbackAmount = -originalAmount; // 기존 조정 되돌리기
                int newAdjustment = saveDto.getAdjustmentAmount(); // 새 조정 적용
                int totalAdjustment = rollbackAmount + newAdjustment;
                
                newBalanceAmt = customer.getBalanceAmt() + totalAdjustment;
                customer.setBalanceAmt(newBalanceAmt);
                
                log.info("잔액 수정 계산 - 기존잔액: {}, 기존조정롤백: {}, 새조정: {}, 총조정: {}, 새잔액: {}", 
                        originalBalanceAmt, rollbackAmount, newAdjustment, totalAdjustment, newBalanceAmt);
            } else {
                // 신규 시: 조정금액만큼 잔액 조정
                newBalanceAmt = customer.getBalanceAmt() + saveDto.getAdjustmentAmount();
                customer.setBalanceAmt(newBalanceAmt);
                
                log.info("잔액 신규 계산 - 기존잔액: {}, 조정금액: {}, 새잔액: {}", 
                        originalBalanceAmt, saveDto.getAdjustmentAmount(), newBalanceAmt);
            }
            customerRepository.save(customer);
            
            // 조정 거래내역 저장
            CustomerAccountTransactions transaction;
            if (isUpdate) {
                // 수정
                transaction = originalTransaction;
                transaction.setCustomerCode(saveDto.getCustomerCode());
                transaction.setTransactionDate(saveDto.getAdjustmentDate());
                transaction.setTransactionType("조정");
                transaction.setAmount(saveDto.getAdjustmentAmount());
                transaction.setBalanceAfter(newBalanceAmt);
                transaction.setReferenceType("수동조정");
                transaction.setReferenceId(saveDto.getTransactionCode().toString());
                transaction.setNote(saveDto.getNote());
                transaction.setDescription("거래처조정수정");
            } else {
                // 신규
                transaction = CustomerAccountTransactions.builder()
                        .customerCode(saveDto.getCustomerCode())
                        .virtualAccountCode(null) // 조정은 가상계좌와 무관
                        .transactionDate(saveDto.getAdjustmentDate())
                        .transactionType("조정")
                        .amount(saveDto.getAdjustmentAmount())
                        .balanceAfter(newBalanceAmt)
                        .referenceType("수동조정")
                        .referenceId("0") // 신규는 임시값, 저장 후 실제 ID로 업데이트
                        .note(saveDto.getNote())
                        .description("거래처조정등록")
                        .build();
            }
            
            transaction = customerAccountTransactionsRepository.save(transaction);
            
            // 신규인 경우 reference_id를 실제 transaction_code로 업데이트
            if (!isUpdate) {
                transaction.setReferenceId(transaction.getTransactionCode().toString());
                customerAccountTransactionsRepository.save(transaction);
            }
            
            String action = isUpdate ? "수정" : "등록";
            log.info("거래처조정처리 {} 완료 - 거래내역코드: {}, 최종잔액: {}", 
                    action, transaction.getTransactionCode(), newBalanceAmt);
            
            String resultMessage = String.format("거래처조정처리가 %s되었습니다. (잔액: %,d원)", action, newBalanceAmt);
            return RespDto.success(resultMessage, transaction.getTransactionCode().toString());
            
        } catch (Exception e) {
            log.error("거래처조정처리 저장 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 거래처조정처리 삭제
     */
    @Transactional
    public RespDto<String> deleteCustomerAdjustment(Integer transactionCode) {
        try {
            log.info("거래처조정처리 삭제 시작 - 거래내역코드: {}", transactionCode);
            
            // 조정 거래내역 조회
            CustomerAccountTransactions transaction = customerAccountTransactionsRepository.findById(transactionCode).orElse(null);
            if (transaction == null) {
                return RespDto.fail("해당 조정내역을 찾을 수 없습니다.");
            }
            
            // 조정 거래유형인지 확인
            if (!"조정".equals(transaction.getTransactionType())) {
                return RespDto.fail("조정 거래내역만 삭제할 수 있습니다.");
            }
            
            // 거래처 조회
            Customer customer = customerRepository.findByCustomerCode(transaction.getCustomerCode());
            if (customer == null) {
                return RespDto.fail("해당 거래처를 찾을 수 없습니다.");
            }
            
            int originalBalanceAmt = customer.getBalanceAmt();
            int adjustmentAmount = transaction.getAmount();
            
            // 거래처 잔액 롤백 (조정금액의 반대만큼 적용)
            int newBalanceAmt = customer.getBalanceAmt() - adjustmentAmount;
            customer.setBalanceAmt(newBalanceAmt);
            customerRepository.save(customer);
            
            log.info("잔액 롤백 계산 - 기존잔액: {}, 조정금액: {}, 롤백후잔액: {}", 
                    originalBalanceAmt, adjustmentAmount, newBalanceAmt);
            
            // 조정 거래내역 삭제
            customerAccountTransactionsRepository.delete(transaction);
            
            log.info("거래처조정처리 삭제 완료 - 거래내역코드: {}, 최종잔액: {}", transactionCode, newBalanceAmt);
            
            String resultMessage = String.format("거래처조정처리가 삭제되었습니다. (잔액: %,d원)", newBalanceAmt);
            return RespDto.success(resultMessage, "삭제 완료");
            
        } catch (Exception e) {
            log.error("거래처조정처리 삭제 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 조정일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatAdjustmentDate(String adjustmentDate) {
        try {
            if (adjustmentDate != null && adjustmentDate.length() == 8) {
                LocalDate date = LocalDate.parse(adjustmentDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return adjustmentDate;
        } catch (Exception e) {
            log.warn("조정일자 포맷 변환 실패: {}", adjustmentDate);
            return adjustmentDate;
        }
    }
}