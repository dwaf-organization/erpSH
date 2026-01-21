package com.inc.sh.service;

import com.inc.sh.dto.transactionStatement.reqDto.TransactionStatementSearchDto;
import com.inc.sh.dto.transactionStatement.respDto.TransactionStatementRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.OrderRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionStatementService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 거래명세서 조회 (수금 계산 포함)
     */
    @Transactional(readOnly = true)
    public RespDto<List<TransactionStatementRespDto>> getTransactionStatement(TransactionStatementSearchDto searchDto) {
        try {
            log.info("거래명세서 조회 시작 - 납기일자: {}, 거래처: {}, 브랜드: {}, 본사: {}", 
                    searchDto.getDeliveryRequestDt(), searchDto.getCustomerCode(), 
                    searchDto.getBrandCode(), searchDto.getHqCode());
            
            // 거래명세서 데이터 조회 (수금 계산 포함)
            List<Object[]> results = orderRepository.findTransactionStatementWithCollection(
                    searchDto.getDeliveryRequestDt(),
                    searchDto.getCustomerCode(),
                    searchDto.getBrandCode(),
                    searchDto.getHqCode()
            );
            
            // DTO 변환
            List<TransactionStatementRespDto> responseList = results.stream()
                    .map(result -> {
                        Integer depositTypeCode = (Integer) result[6];
                        String depositType = (depositTypeCode == 1) ? "충전형" : "후입금";
                        
                        return TransactionStatementRespDto.builder()
                                .orderNo((String) result[0])                           // 주문번호
                                .customerName((String) result[1])                      // 거래처명
                                .bizNum((String) result[2])                            // 사업자번호
                                .supplyAmt(((Number) result[3]).intValue())            // 공급가
                                .vatAmt(((Number) result[4]).intValue())               // 부가세
                                .totalAmt(((Number) result[5]).intValue())             // 합계금액
                                .todayCollection(((Number) result[7]).intValue())      // 당일수금 (계산됨)
                                .uncollectedBalance(((Number) result[8]).intValue())   // 미수잔액 (계산됨)
                                .origin("국내산")                                      // 원산지 (임시값)
                                .depositType(depositType)                              // 입금유형
                                .build();
                    })
                    .collect(Collectors.toList());
            
            // 합계 계산
            int totalSupplyAmt = responseList.stream().mapToInt(TransactionStatementRespDto::getSupplyAmt).sum();
            int totalVatAmt = responseList.stream().mapToInt(TransactionStatementRespDto::getVatAmt).sum();
            int totalAmt = responseList.stream().mapToInt(TransactionStatementRespDto::getTotalAmt).sum();
            int totalTodayCollection = responseList.stream().mapToInt(TransactionStatementRespDto::getTodayCollection).sum();
            int totalUncollectedBalance = responseList.stream().mapToInt(TransactionStatementRespDto::getUncollectedBalance).sum();
            
            log.info("거래명세서 조회 완료 - 조회건수: {}, 합계금액: {}, 당일수금: {}, 미수잔액: {}", 
                    responseList.size(), totalAmt, totalTodayCollection, totalUncollectedBalance);
            
            return RespDto.success("거래명세서 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("거래명세서 조회 중 오류 발생 - 납기일자: {}", searchDto.getDeliveryRequestDt(), e);
            return RespDto.fail("거래명세서 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래명세서 요약 정보 조회 (합계)
     */
    @Transactional(readOnly = true)
    public RespDto<TransactionStatementSummaryDto> getTransactionStatementSummary(TransactionStatementSearchDto searchDto) {
        try {
            log.info("거래명세서 요약 조회 시작 - 납기일자: {}", searchDto.getDeliveryRequestDt());
            
            List<Object[]> results = orderRepository.findTransactionStatementWithCollection(
                    searchDto.getDeliveryRequestDt(),
                    searchDto.getCustomerCode(),
                    searchDto.getBrandCode(),
                    searchDto.getHqCode()
            );
            
            // 합계 계산
            int totalSupplyAmt = 0;
            int totalVatAmt = 0;
            int totalAmt = 0;
            int totalTodayCollection = 0;
            int totalUncollectedBalance = 0;
            
            for (Object[] result : results) {
                totalSupplyAmt += ((Number) result[3]).intValue();
                totalVatAmt += ((Number) result[4]).intValue();
                totalAmt += ((Number) result[5]).intValue();
                totalTodayCollection += ((Number) result[7]).intValue();
                totalUncollectedBalance += ((Number) result[8]).intValue();
            }
            
            TransactionStatementSummaryDto summary = TransactionStatementSummaryDto.builder()
                    .totalOrderCount(results.size())
                    .totalSupplyAmt(totalSupplyAmt)
                    .totalVatAmt(totalVatAmt)
                    .totalAmt(totalAmt)
                    .totalTodayCollection(totalTodayCollection)
                    .totalUncollectedBalance(totalUncollectedBalance)
                    .build();
            
            log.info("거래명세서 요약 조회 완료 - 주문건수: {}, 합계금액: {}", results.size(), totalAmt);
            
            return RespDto.success("거래명세서 요약 조회 성공", summary);
            
        } catch (Exception e) {
            log.error("거래명세서 요약 조회 중 오류 발생", e);
            return RespDto.fail("거래명세서 요약 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래명세서 요약 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionStatementSummaryDto {
        private int totalOrderCount;       // 총 주문 건수
        private int totalSupplyAmt;        // 공급가 합계
        private int totalVatAmt;           // 부가세 합계
        private int totalAmt;              // 합계금액 합계
        private int totalTodayCollection;  // 당일수금 합계
        private int totalUncollectedBalance; // 미수잔액 합계
    }
    
}