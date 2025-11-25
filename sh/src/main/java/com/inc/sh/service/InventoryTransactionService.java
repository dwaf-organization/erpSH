package com.inc.sh.service;

import com.inc.sh.dto.inventoryTransaction.reqDto.InventoryTransactionSearchDto;
import com.inc.sh.dto.inventoryTransaction.respDto.InventoryTransactionRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.InventoryTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryTransactionService {
    
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    
    /**
     * 재고수불부 조회
     * 조회일자 + 창고코드 + 분류코드 + 품목코드 조건으로 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<InventoryTransactionRespDto>> getInventoryTransactionSummary(InventoryTransactionSearchDto searchDto) {
        try {
            log.info("재고수불부 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = inventoryTransactionsRepository.findInventoryTransactionSummary(
                    searchDto.getStartDate(),
                    searchDto.getEndDate(),
                    searchDto.getWarehouseCode(),
                    searchDto.getCategoryCode(),
                    searchDto.getItemCodeSearch()
            );
            
            List<InventoryTransactionRespDto> responseList = results.stream()
                    .map(result -> InventoryTransactionRespDto.builder()
                            .warehouseCode((Integer) result[0])
                            .categoryName((String) result[1])
                            .itemCode((Integer) result[2])
                            .itemName((String) result[3])
                            .specification((String) result[4])
                            .unit((String) result[5])
                            .transactionDate(formatTransactionDate((String) result[6]))
                            .transactionType((String) result[7])
                            .quantity(((Number) result[8]).intValue())
                            .unitPrice(((Number) result[9]).intValue())
                            .amount(((Number) result[10]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("재고수불부 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("재고수불부 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("재고수불부 조회 중 오류 발생", e);
            return RespDto.fail("재고수불부 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 거래일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatTransactionDate(String transactionDate) {
        try {
            if (transactionDate != null && transactionDate.length() == 8) {
                LocalDate date = LocalDate.parse(transactionDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return transactionDate;
        } catch (Exception e) {
            log.warn("거래일자 포맷 변환 실패: {}", transactionDate);
            return transactionDate;
        }
    }
}