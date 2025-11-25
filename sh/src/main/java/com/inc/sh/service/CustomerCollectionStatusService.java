package com.inc.sh.service;

import com.inc.sh.dto.customerCollectionStatus.reqDto.CustomerCollectionStatusSearchDto;
import com.inc.sh.dto.customerCollectionStatus.respDto.CustomerCollectionStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.CustomerAccountTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerCollectionStatusService {
    
    private final CustomerAccountTransactionsRepository customerAccountTransactionsRepository;
    
    /**
     * 거래처별잔액현황 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<CustomerCollectionStatusRespDto>> getCustomerCollectionStatusList(CustomerCollectionStatusSearchDto searchDto) {
        try {
            log.info("거래처별잔액현황 조회 시작 - 조건: {}", searchDto);
            
            // 1. 기간 내 거래가 있는 거래처들의 집계 데이터 조회
            List<Object[]> transactionSummaries = customerAccountTransactionsRepository.findAllCustomerTransactionSummary(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getCustomerCode()
            );
            
            List<CustomerCollectionStatusRespDto> responseList = new ArrayList<>();
            
            for (Object[] summary : transactionSummaries) {
                Integer customerCode = (Integer) summary[0];
                String customerName = (String) summary[1];
                Integer creditLimit = summary[2] != null ? ((Number) summary[2]).intValue() : 0;
                
                // 거래 집계 데이터
                int salesAmountRaw = summary[3] != null ? ((Number) summary[3]).intValue() : 0;
                int returnAmount = summary[4] != null ? ((Number) summary[4]).intValue() : 0;
                int depositAmount = summary[5] != null ? ((Number) summary[5]).intValue() : 0;
                int adjustmentAmount = summary[6] != null ? ((Number) summary[6]).intValue() : 0;
                
                // 2. 전일잔액 조회 (시작일 이전 최근 잔액)
                Object[] previousBalanceResult = customerAccountTransactionsRepository.findPreviousBalanceByCustomerCode(
                        customerCode, searchDto.getStartDate());
                int previousBalance = 0;
                if (previousBalanceResult != null && previousBalanceResult.length > 1 && previousBalanceResult[1] != null) {
                    previousBalance = ((Number) previousBalanceResult[1]).intValue();
                }
                
                log.debug("거래처 {} 전일잔액 조회 결과: {}", customerCode, previousBalance);
                
                // 3. 매출액 계산 (출금+외상-반품입금, 모두 양수로 처리)
                int salesAmount = salesAmountRaw - returnAmount;
                
                // 4. 잔액 계산 (전일잔액 + 입금액 + 조정액 - 매출액)
                int currentBalance = previousBalance + depositAmount + adjustmentAmount - salesAmount;
                
                CustomerCollectionStatusRespDto dto = CustomerCollectionStatusRespDto.builder()
                        .customerCode(customerCode)
                        .customerName(customerName)
                        .creditLimit(creditLimit)
                        .previousBalance(previousBalance)
                        .salesAmount(salesAmount)
                        .depositAmount(depositAmount)
                        .adjustmentAmount(adjustmentAmount)
                        .currentBalance(currentBalance)
                        .build();
                
                responseList.add(dto);
                
                log.debug("거래처 {} 계산 완료 - 전일잔액: {}, 매출액: {}, 입금액: {}, 조정액: {}, 현재잔액: {}", 
                        customerCode, previousBalance, salesAmount, depositAmount, adjustmentAmount, currentBalance);
            }
            
            log.info("거래처별잔액현황 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("거래처별잔액현황 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래처별잔액현황 조회 중 오류 발생", e);
            return RespDto.fail("거래처별잔액현황 조회 중 오류가 발생했습니다.");
        }
    }
}