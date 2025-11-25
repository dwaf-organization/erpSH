package com.inc.sh.service.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.charge.reqDto.AppTransactionHistoryReqDto;
import com.inc.sh.dto.charge.respDto.AppTransactionHistoryRespDto;
import com.inc.sh.dto.charge.respDto.AppTransactionRespDto;
import com.inc.sh.dto.charge.respDto.AppTransactionSummaryRespDto;
import com.inc.sh.entity.CustomerAccountTransactions;
import com.inc.sh.repository.CustomerAccountTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppChargeService {
    
    private final CustomerAccountTransactionsRepository transactionRepository;
    
    /**
     * [앱] 거래내역조회 (페이징)
     */
    @Transactional(readOnly = true)
    public RespDto<AppTransactionHistoryRespDto> getTransactionHistory(AppTransactionHistoryReqDto request) {
        try {
            // 1. 거래내역 전체 조회 (JpaRepository 기본 메서드 사용)
            List<CustomerAccountTransactions> allTransactions = transactionRepository.findAll();
            
            // 2. 거래처별 + 날짜 범위 필터링 및 정렬
            List<CustomerAccountTransactions> filteredTransactions = allTransactions.stream()
                    .filter(transaction -> 
                        transaction.getCustomerCode().equals(request.getCustomerCode()) &&
                        transaction.getTransactionDate().compareTo(request.getStartTransactionDate()) >= 0 && 
                        transaction.getTransactionDate().compareTo(request.getEndTransactionDate()) <= 0
                    )
                    .sorted((t1, t2) -> {
                        // 거래일자 최신순 정렬
                        int dateCompare = t2.getTransactionDate().compareTo(t1.getTransactionDate());
                        if (dateCompare != 0) return dateCompare;
                        // 같은 날이면 createdAt 또는 다른 필드로 정렬
                        if (t1.getCreatedAt() != null && t2.getCreatedAt() != null) {
                            return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                        }
                        return 0;
                    })
                    .toList();
            
            // 3. 페이징 처리
            int totalCount = filteredTransactions.size();
            int startIndex = request.getPage() * request.getSize();
            int endIndex = Math.min(startIndex + request.getSize(), totalCount);
            
            List<CustomerAccountTransactions> pagedTransactions = (startIndex < totalCount) 
                    ? filteredTransactions.subList(startIndex, endIndex)
                    : List.of();
            
            // 4. 합계 계산 (전체 데이터 기준)
            AppTransactionSummaryRespDto summary = calculateSummary(filteredTransactions);
            
            // 5. DTO 변환
            List<AppTransactionRespDto> transactionList = pagedTransactions.stream()
                    .map(this::convertToTransactionDto)
                    .toList();
            
            // 6. 페이징 정보 계산
            int totalPages = (int) Math.ceil((double) totalCount / request.getSize());
            boolean hasNext = (request.getPage() + 1) < totalPages;
            
            // 7. 응답 데이터 생성
            AppTransactionHistoryRespDto response = AppTransactionHistoryRespDto.builder()
                    .summary(summary)
                    .currentPage(request.getPage())
                    .totalPages(totalPages)
                    .hasNext(hasNext)
                    .transactions(transactionList)
                    .build();
            
            log.info("[앱] 거래내역조회 완료 - customerCode: {}, 기간: {} ~ {}, 조회건수: {}, 총건수: {}", 
                    request.getCustomerCode(), request.getStartTransactionDate(), request.getEndTransactionDate(), 
                    transactionList.size(), totalCount);
            
            return RespDto.success("거래내역 조회 성공", response);
            
        } catch (Exception e) {
            log.error("거래내역조회 실패 - customerCode: {}, 기간: {} ~ {}", 
                    request.getCustomerCode(), request.getStartTransactionDate(), request.getEndTransactionDate(), e);
            return RespDto.fail("거래내역 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 거래내역 합계 계산
     */
    private AppTransactionSummaryRespDto calculateSummary(List<CustomerAccountTransactions> transactions) {
        int totalDeposit = 0;      // 입금 합계
        int totalOrder = 0;        // 출금 합계 (절댓값)
        int totalAdjustment = 0;   // 조정 합계
        int totalReturn = 0;       // 반품입금 합계
        
        for (CustomerAccountTransactions transaction : transactions) {
            String transactionType = transaction.getTransactionType();
            Integer amount = transaction.getAmount();
            
            if (amount == null) continue;
            
            switch (transactionType) {
                case "입금":
                    totalDeposit += amount;
                    break;
                case "출금":
                    totalOrder += Math.abs(amount); // 절댓값
                    break;
                case "조정":
                    totalAdjustment += amount;
                    break;
                case "반품입금":
                    totalReturn += amount;
                    break;
            }
        }
        
        return AppTransactionSummaryRespDto.builder()
                .totalDeposit(totalDeposit)
                .totalOrder(totalOrder)
                .totalAdjustment(totalAdjustment)
                .totalReturn(totalReturn)
                .totalCount((long) transactions.size())
                .build();
    }
    
    /**
     * CustomerAccountTransactions -> AppTransactionRespDto 변환
     * 날짜 형식: yyyyMMdd -> YYYY-MM-DD
     */
    private AppTransactionRespDto convertToTransactionDto(CustomerAccountTransactions transaction) {
        // yyyyMMdd -> YYYY-MM-DD 변환
        String formattedDate = formatDate(transaction.getTransactionDate());
        
        return AppTransactionRespDto.builder()
                .transactionDate(formattedDate)
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .build();
    }
    
    /**
     * 날짜 형식 변환: yyyyMMdd -> YYYY-MM-DD
     */
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() != 8) {
            return dateStr;
        }
        // 20241125 -> 2024-11-25
        return dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8);
    }
}