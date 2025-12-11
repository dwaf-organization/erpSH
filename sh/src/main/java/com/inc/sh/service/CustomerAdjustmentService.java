package com.inc.sh.service;

import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSearchDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentDeleteReqDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSaveReqDto;
import com.inc.sh.dto.customerAdjustment.respDto.CustomerAdjustmentBatchResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerAdjustmentService {
    
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    private final CustomerRepository customerRepository;
    
    /**
     * 거래처조정처리 조회 (hqCode 검증 포함, 날짜 범위 검색)
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerAdjustmentRespDto>> getCustomerAdjustmentList(CustomerAdjustmentSearchDto searchDto) {
        try {
            log.info("거래처조정처리 조회 시작 - hqCode: {}, customerCode: {}, 날짜범위: {}~{}", 
                    searchDto.getHqCode(), searchDto.getCustomerCode(), 
                    searchDto.getAdjustmentDateStart(), searchDto.getAdjustmentDateEnd());
            
            // hqCode 필수 검증
            if (searchDto.getHqCode() == null) {
                return RespDto.fail("본사코드는 필수 파라미터입니다.");
            }
            
            // 날짜 범위 검증 (시작일이 종료일보다 큰 경우)
            if (searchDto.getAdjustmentDateStart() != null && searchDto.getAdjustmentDateEnd() != null) {
                if (searchDto.getAdjustmentDateStart().compareTo(searchDto.getAdjustmentDateEnd()) > 0) {
                    return RespDto.fail("시작일이 종료일보다 클 수 없습니다.");
                }
            }
            
            // 날짜 범위 포함 조회
            List<Object[]> results = customerAccountTransactionsRepository.findCustomerAdjustmentsWithDateRange(
                    searchDto.getHqCode(),
                    searchDto.getCustomerCode(),
                    searchDto.getAdjustmentDateStart(),     // ✅ 시작일
                    searchDto.getAdjustmentDateEnd()        // ✅ 종료일
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
            
            log.info("거래처조정처리 조회 완료 - hqCode: {}, 조회 건수: {}, 날짜범위: {}~{}", 
                    searchDto.getHqCode(), responseList.size(),
                    searchDto.getAdjustmentDateStart(), searchDto.getAdjustmentDateEnd());
            return RespDto.success("거래처조정처리 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처조정처리 조회 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래처조정처리 다중 저장 (신규/수정)
     */
    @Transactional
    public RespDto<CustomerAdjustmentBatchResult> saveCustomerAdjustments(CustomerAdjustmentSaveReqDto request) {
        try {
            log.info("거래처조정처리 다중 저장 시작 - 총 {}건", 
                    request.getAdjustments() != null ? request.getAdjustments().size() : 0);
            
            // 요청 데이터 검증
            if (request.getAdjustments() == null || request.getAdjustments().isEmpty()) {
                return RespDto.fail("저장할 조정처리 데이터가 없습니다.");
            }
            
            List<CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult> successList = new ArrayList<>();
            List<CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult> failureList = new ArrayList<>();
            
            // 개별 저장 처리
            for (CustomerAdjustmentSaveReqDto.CustomerAdjustmentSaveItemDto saveDto : request.getAdjustments()) {
                try {
                    // 기본값 설정
                    if (saveDto.getTransactionType() == null || saveDto.getTransactionType().isEmpty()) {
                        saveDto.setTransactionType("조정");
                    }
                    
                    // 필수 필드 검증
                    if (saveDto.getCustomerCode() == null) {
                        throw new RuntimeException("거래처코드는 필수입니다.");
                    }
                    if (saveDto.getAdjustmentDate() == null || saveDto.getAdjustmentDate().trim().isEmpty()) {
                        throw new RuntimeException("조정일자는 필수입니다.");
                    }
                    if (saveDto.getAdjustmentAmount() == null) {
                        throw new RuntimeException("조정금액은 필수입니다.");
                    }
                    
                    CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult result = saveSingleAdjustment(saveDto);
                    if (result != null) {
                        successList.add(result);
                        log.info("조정 저장 성공 - 거래내역코드: {}, 거래처: {}", 
                                result.getTransactionCode(), result.getCustomerCode());
                    }
                } catch (Exception e) {
                    CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult failure = 
                        CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult.builder()
                                .transactionCode(saveDto.getTransactionCode())
                                .customerCode(saveDto.getCustomerCode())
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("조정 저장 실패 - 거래처: {}, 원인: {}", saveDto.getCustomerCode(), e.getMessage());
                }
            }
            
            // 결과 집계
            CustomerAdjustmentBatchResult batchResult = CustomerAdjustmentBatchResult.builder()
                    .totalCount(request.getAdjustments().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("거래처조정처리 다중 저장 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("거래처조정처리 다중 저장 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("거래처조정처리 다중 저장 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 다중 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 개별 조정 저장 처리
     */
    private CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult saveSingleAdjustment(
            CustomerAdjustmentSaveReqDto.CustomerAdjustmentSaveItemDto saveDto) {
        
        // 거래처 조회
        Customer customer = customerRepository.findByCustomerCode(saveDto.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다.");
        }
        
        boolean isUpdate = saveDto.getTransactionCode() != null;
        CustomerAccountTransactions originalTransaction = null;
        int originalAmount = 0;
        int originalBalanceAmt = customer.getBalanceAmt(); // 수정 전 잔액
        
        // 수정인 경우 기존 조정 정보 조회
        if (isUpdate) {
            originalTransaction = customerAccountTransactionsRepository.findById(saveDto.getTransactionCode()).orElse(null);
            if (originalTransaction == null) {
                throw new RuntimeException("해당 조정내역을 찾을 수 없습니다.");
            }
            originalAmount = originalTransaction.getAmount();
        }
        
        // 거래처 잔액 업데이트
        int newBalanceAmt;
        if (isUpdate) {
            // 수정 시: 기존 조정금액 되돌리고 새 조정금액 적용
            int rollbackAmount = -originalAmount; // 기존 조정 되돌리기
            int newAdjustment = saveDto.getAdjustmentAmount(); // 새 조정 적용
            int totalAdjustment = rollbackAmount + newAdjustment;
            
            newBalanceAmt = customer.getBalanceAmt() + totalAdjustment;
            customer.setBalanceAmt(newBalanceAmt);
            
            log.info("개별 잔액 수정 계산 - 기존잔액: {}, 기존조정롤백: {}, 새조정: {}, 이조정: {}, 새잔액: {}", 
                    originalBalanceAmt, rollbackAmount, newAdjustment, totalAdjustment, newBalanceAmt);
        } else {
            // 신규 시: 조정금액만큼 잔액 조정
            newBalanceAmt = customer.getBalanceAmt() + saveDto.getAdjustmentAmount();
            customer.setBalanceAmt(newBalanceAmt);
            
            log.info("개별 잔액 신규 계산 - 기존잔액: {}, 조정금액: {}, 새잔액: {}", 
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
        log.info("개별 조정 {} 완료 - 거래내역코드: {}, 거래처: {}, 최종잔액: {}", 
                action, transaction.getTransactionCode(), customer.getCustomerName(), newBalanceAmt);
        
        return CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult.builder()
                .transactionCode(transaction.getTransactionCode())
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .adjustmentAmount(saveDto.getAdjustmentAmount())
                .finalBalance(newBalanceAmt)
                .message(String.format("%s 완료 (잔액: %,d원)", action, newBalanceAmt))
                .build();
    }
    
    /**
     * 거래처조정처리 다중 삭제
     */
    @Transactional
    public RespDto<CustomerAdjustmentBatchResult> deleteCustomerAdjustments(CustomerAdjustmentDeleteReqDto request) {
        try {
            log.info("거래처조정처리 다중 삭제 시작 - 총 {}건", 
                    request.getTransactionCodes() != null ? request.getTransactionCodes().size() : 0);
            
            // 요청 데이터 검증
            if (request.getTransactionCodes() == null || request.getTransactionCodes().isEmpty()) {
                return RespDto.fail("삭제할 거래내역코드가 없습니다.");
            }
            
            List<CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult> successList = new ArrayList<>();
            List<CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult> failureList = new ArrayList<>();
            
            // 개별 삭제 처리
            for (Integer transactionCode : request.getTransactionCodes()) {
                try {
                    CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult result = deleteSingleAdjustment(transactionCode);
                    if (result != null) {
                        successList.add(result);
                        log.info("조정 삭제 성공 - 거래내역코드: {}", transactionCode);
                    }
                } catch (Exception e) {
                    CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult failure = 
                        CustomerAdjustmentBatchResult.CustomerAdjustmentFailureResult.builder()
                                .transactionCode(transactionCode)
                                .reason(e.getMessage())
                                .build();
                    failureList.add(failure);
                    log.error("조정 삭제 실패 - 거래내역코드: {}, 원인: {}", transactionCode, e.getMessage());
                }
            }
            
            // 결과 집계
            CustomerAdjustmentBatchResult batchResult = CustomerAdjustmentBatchResult.builder()
                    .totalCount(request.getTransactionCodes().size())
                    .successCount(successList.size())
                    .failureCount(failureList.size())
                    .successList(successList)
                    .failureList(failureList)
                    .build();
            
            log.info("거래처조정처리 다중 삭제 완료 - 총:{}건, 성공:{}건, 실패:{}건", 
                    batchResult.getTotalCount(), batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            String resultMessage = String.format("거래처조정처리 다중 삭제 완료 (성공: %d건, 실패: %d건)", 
                    batchResult.getSuccessCount(), batchResult.getFailureCount());
            
            return RespDto.success(resultMessage, batchResult);
            
        } catch (Exception e) {
            log.error("거래처조정처리 다중 삭제 중 오류 발생", e);
            return RespDto.fail("거래처조정처리 다중 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 개별 조정 삭제 처리
     */
    private CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult deleteSingleAdjustment(Integer transactionCode) {
        // 조정 거래내역 조회
        CustomerAccountTransactions transaction = customerAccountTransactionsRepository.findById(transactionCode).orElse(null);
        if (transaction == null) {
            throw new RuntimeException("해당 조정내역을 찾을 수 없습니다.");
        }
        
        // 조정 거래유형인지 확인
        if (!"조정".equals(transaction.getTransactionType())) {
            throw new RuntimeException("조정 거래내역만 삭제할 수 있습니다.");
        }
        
        // 거래처 조회
        Customer customer = customerRepository.findByCustomerCode(transaction.getCustomerCode());
        if (customer == null) {
            throw new RuntimeException("해당 거래처를 찾을 수 없습니다.");
        }
        
        int originalBalanceAmt = customer.getBalanceAmt();
        int adjustmentAmount = transaction.getAmount();
        
        // 거래처 잔액 롤백 (조정금액의 반대만큼 적용)
        int newBalanceAmt = customer.getBalanceAmt() - adjustmentAmount;
        customer.setBalanceAmt(newBalanceAmt);
        customerRepository.save(customer);
        
        // 조정 거래내역 삭제
        customerAccountTransactionsRepository.delete(transaction);
        
        log.info("개별 조정 삭제 완료 - 거래내역코드: {}, 거래처: {}, 기존잔액: {}, 롤백금액: {}, 최종잔액: {}", 
                transactionCode, customer.getCustomerName(), originalBalanceAmt, adjustmentAmount, newBalanceAmt);
        
        return CustomerAdjustmentBatchResult.CustomerAdjustmentSuccessResult.builder()
                .transactionCode(transactionCode)
                .customerCode(customer.getCustomerCode())
                .customerName(customer.getCustomerName())
                .adjustmentAmount(adjustmentAmount)
                .finalBalance(newBalanceAmt)
                .message(String.format("삭제 완료 (잔액: %,d원)", newBalanceAmt))
                .build();
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