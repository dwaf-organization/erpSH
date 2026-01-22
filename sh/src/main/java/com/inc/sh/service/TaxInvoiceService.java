package com.inc.sh.service;

import com.inc.sh.dto.taxInvoice.reqDto.TaxInvoiceReqDto;
import com.inc.sh.dto.taxInvoice.respDto.TaxInvoiceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.TaxInvoiceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxInvoiceService {
    
    private final TaxInvoiceOrderRepository taxInvoiceOrderRepository;
    
    /**
     * 전자세금계산서 발행 데이터 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<TaxInvoiceRespDto>> getTaxInvoiceData(TaxInvoiceReqDto reqDto) {
        try {
            log.info("전자세금계산서 발행 데이터 조회 시작 - hqCode: {}, startDate: {}, endDate: {}, customerCode: {}, itemCodes: {}", 
                    reqDto.getHqCode(), reqDto.getStartDate(), reqDto.getEndDate(), 
                    reqDto.getCustomerCode(), reqDto.getItemCodes());
            
            // 품목코드 리스트 처리
            List<Integer> itemCodes = reqDto.getItemCodes();
            if (itemCodes == null) {
                itemCodes = new ArrayList<>();
            }
            int itemCodesSize = itemCodes.size();
            
            // 1. 배송완료된 주문에서 매출 데이터 조회
            List<Object[]> salesData = taxInvoiceOrderRepository.findTaxInvoiceDataByConditions(
                    reqDto.getHqCode(),
                    reqDto.getStartDate(),
                    reqDto.getEndDate(),
                    reqDto.getCustomerCode(),
                    itemCodes,
                    itemCodesSize
            );
            
            // 2. 승인된 반품에서 반품 데이터 조회
            List<Object[]> returnData = taxInvoiceOrderRepository.findReturnDataByConditions(
                    reqDto.getHqCode(),
                    reqDto.getStartDate(),
                    reqDto.getEndDate(),
                    reqDto.getCustomerCode(),
                    itemCodes,
                    itemCodesSize
            );
            
            // 3. 품목명 정보 조회
            List<Object[]> itemNameData = taxInvoiceOrderRepository.findItemNamesByConditions(
                    reqDto.getHqCode(),
                    reqDto.getStartDate(),
                    reqDto.getEndDate(),
                    reqDto.getCustomerCode(),
                    itemCodes,
                    itemCodesSize
            );
            
            // 4. 데이터 병합 및 DTO 생성
            List<TaxInvoiceRespDto> result = mergeSalesAndReturnData(salesData, returnData, itemNameData);
            
            log.info("전자세금계산서 발행 데이터 조회 완료 - 결과 건수: {}", result.size());
            
            return RespDto.success("전자세금계산서 발행 데이터 조회 완료", result);
            
        } catch (Exception e) {
            log.error("전자세금계산서 발행 데이터 조회 중 오류 발생", e);
            return RespDto.fail("전자세금계산서 발행 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 매출 데이터와 반품 데이터를 병합하여 최종 DTO 생성
     */
    private List<TaxInvoiceRespDto> mergeSalesAndReturnData(List<Object[]> salesData, 
                                                           List<Object[]> returnData, 
                                                           List<Object[]> itemNameData) {
        
        // 반품 데이터를 거래처코드별로 매핑
        Map<Integer, Object[]> returnMap = returnData.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0], // customer_code
                    row -> row,
                    (existing, replacement) -> existing
                ));
        
        // 품목명 데이터를 거래처코드별로 매핑
        Map<Integer, Object[]> itemNameMap = itemNameData.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0], // customer_code
                    row -> row,
                    (existing, replacement) -> existing
                ));
        
        List<TaxInvoiceRespDto> result = new ArrayList<>();
        
        for (Object[] salesRow : salesData) {
            Integer customerCode = (Integer) salesRow[0];
            String customerName = (String) salesRow[1];
            String ownerName = (String) salesRow[2];
            String bizNum = (String) salesRow[3];
            String addr = (String) salesRow[4];
            // salesRow[5]는 GROUP_CONCAT 결과이지만 더 정확한 품목명 처리를 위해 별도 조회 사용
            
            Integer salesTaxFreeAmt = ((Number) salesRow[6]).intValue();
            Integer salesTaxableAmt = ((Number) salesRow[7]).intValue();
            Integer salesVatAmt = ((Number) salesRow[8]).intValue();
            
            // 해당 거래처의 반품 데이터 가져오기
            Object[] returnRow = returnMap.get(customerCode);
            Integer returnTaxFreeAmt = 0;
            Integer returnTaxableAmt = 0;
            Integer returnVatAmt = 0;
            
            if (returnRow != null) {
                returnTaxFreeAmt = ((Number) returnRow[1]).intValue();
                returnTaxableAmt = ((Number) returnRow[2]).intValue();
                returnVatAmt = ((Number) returnRow[3]).intValue();
            }
            
            // 최종 매출 계산 (매출 - 반품)
            Integer finalTaxFreeAmt = Math.max(0, salesTaxFreeAmt - returnTaxFreeAmt);
            Integer finalTaxableAmt = Math.max(0, salesTaxableAmt - returnTaxableAmt);
            Integer finalVatAmt = Math.max(0, salesVatAmt - returnVatAmt);
            
            // 품목명 처리
            String itemNames = formatItemNames(itemNameMap.get(customerCode));
            
            // DTO 생성
            TaxInvoiceRespDto dto = TaxInvoiceRespDto.fromQueryResult(
                    customerCode, customerName, ownerName, bizNum, addr, itemNames,
                    finalTaxFreeAmt, finalTaxableAmt, finalVatAmt
            );
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 품목명을 "첫품목 외 N개" 형식으로 포맷팅
     */
    private String formatItemNames(Object[] itemNameRow) {
        if (itemNameRow == null) {
            return "";
        }
        
        Integer itemCount = ((Number) itemNameRow[1]).intValue();
        String firstItemName = (String) itemNameRow[2];
        
        if (itemCount <= 1) {
            return firstItemName != null ? firstItemName : "";
        } else {
            return firstItemName + " 외 " + (itemCount - 1) + "개";
        }
    }
}