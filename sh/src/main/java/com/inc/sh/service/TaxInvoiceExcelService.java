package com.inc.sh.service;

import com.inc.sh.dto.taxInvoice.reqDto.TaxInvoiceExcelReqDto;
import com.inc.sh.dto.taxInvoice.respDto.TaxInvoiceExcelRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.TaxInvoiceExcelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaxInvoiceExcelService {
    
    private final TaxInvoiceExcelRepository taxInvoiceExcelRepository;
    
    /**
     * 전자세금계산서 엑셀 생성
     */
    @Transactional(readOnly = true)
    public RespDto<byte[]> generateTaxInvoiceExcel(TaxInvoiceExcelReqDto reqDto) {
        try {
            log.info("전자세금계산서 엑셀 생성 시작 - hqCode: {}, startDate: {}, endDate: {}, taxType: {}, issueDate: {}", 
                    reqDto.getHqCode(), reqDto.getStartDate(), reqDto.getEndDate(), 
                    reqDto.getTaxType(), reqDto.getIssueDate());
            
            // 엑셀 데이터 조회
            List<TaxInvoiceExcelRespDto> excelData = getTaxInvoiceExcelData(reqDto);
            
            if (excelData.isEmpty()) {
                return RespDto.fail("조회된 데이터가 없습니다.");
            }
            
            // 엑셀 파일 생성
            byte[] excelFile = createExcelFile(excelData);
            
            log.info("전자세금계산서 엑셀 생성 완료 - 데이터 건수: {}, 파일 크기: {} bytes", 
                    excelData.size(), excelFile.length);
            
            return RespDto.success("전자세금계산서 엑셀 생성 완료", excelFile);
            
        } catch (Exception e) {
            log.error("전자세금계산서 엑셀 생성 중 오류 발생", e);
            return RespDto.fail("전자세금계산서 엑셀 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 전자세금계산서 엑셀용 데이터 조회
     */
    private List<TaxInvoiceExcelRespDto> getTaxInvoiceExcelData(TaxInvoiceExcelReqDto reqDto) {
        // 품목코드 리스트 처리
        List<Integer> itemCodes = reqDto.getItemCodes();
        if (itemCodes == null) {
            itemCodes = new ArrayList<>();
        }
        int itemCodesSize = itemCodes.size();
        
        // 1. 배송완료된 주문에서 매출 데이터 조회 (본사정보 포함)
        List<Object[]> salesData = taxInvoiceExcelRepository.findTaxInvoiceExcelDataByConditions(
                reqDto.getHqCode(),
                reqDto.getStartDate(),
                reqDto.getEndDate(),
                reqDto.getCustomerCode(),
                itemCodes,
                itemCodesSize
        );
        
        // 2. 승인된 반품에서 반품 데이터 조회
        List<Object[]> returnData = taxInvoiceExcelRepository.findReturnExcelDataByConditions(
                reqDto.getHqCode(),
                reqDto.getStartDate(),
                reqDto.getEndDate(),
                reqDto.getCustomerCode(),
                itemCodes,
                itemCodesSize
        );
        
        // 3. 데이터 병합 및 DTO 생성
        return mergeExcelData(salesData, returnData, reqDto.getTaxType(), reqDto.getIssueDate());
    }
    
    /**
     * 매출 데이터와 반품 데이터를 병합하여 엑셀용 DTO 생성
     */
    private List<TaxInvoiceExcelRespDto> mergeExcelData(List<Object[]> salesData, 
                                                       List<Object[]> returnData,
                                                       String taxType,
                                                       String issueDate) {
        
        // 반품 데이터를 거래처코드별로 매핑
        Map<Integer, Object[]> returnMap = returnData.stream()
                .collect(Collectors.toMap(
                    row -> (Integer) row[0], // customer_code
                    row -> row,
                    (existing, replacement) -> existing
                ));
        
        List<TaxInvoiceExcelRespDto> result = new ArrayList<>();
        
        for (Object[] salesRow : salesData) {
            // 본사 정보
            String hqBizNum = (String) salesRow[0];
            String supplierCompanyName = (String) salesRow[1];
            String supplierCeoName = (String) salesRow[2];
            String supplierAddr = (String) salesRow[3];
            String supplierBizType = (String) salesRow[4];
            String supplierBizSector = (String) salesRow[5];
            String supplierEmail = null;
            
            // 거래처 정보
            Integer customerCode = (Integer) salesRow[6];
            String customerBizNum = (String) salesRow[7];
            String customerName = (String) salesRow[8];
            String customerOwnerName = (String) salesRow[9];
            String customerAddr = (String) salesRow[10];
            String customerBizType = (String) salesRow[11];
            String customerBizSector = (String) salesRow[12];
            String customerEmail = (String) salesRow[13];
            String itemNames = (String) salesRow[14];
            
            // 매출 금액
            Integer salesTaxFreeAmt = ((Number) salesRow[15]).intValue();
            Integer salesTaxableAmt = ((Number) salesRow[16]).intValue();
            Integer salesVatAmt = ((Number) salesRow[17]).intValue();
            
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
            
            // 공급가액 계산 (면세/과세에 따라)
            Integer supplyAmount;
            if ("면세".equals(taxType)) {
                supplyAmount = finalTaxFreeAmt;
            } else { // "과세"
                supplyAmount = finalTaxableAmt;
            }
            
            // DTO 생성
            TaxInvoiceExcelRespDto dto = TaxInvoiceExcelRespDto.fromQueryResult(
                    hqBizNum, supplierCompanyName, supplierCeoName, supplierAddr, 
                    supplierBizType, supplierBizSector, supplierEmail,
                    customerBizNum, customerName, customerOwnerName, customerAddr, customerBizType, customerBizSector, customerEmail,
                    formatItemNames(itemNames), supplyAmount, issueDate
            );
            
            result.add(dto);
        }
        
        return result;
    }
    
    /**
     * 품목명을 "첫품목 외 N개" 형식으로 포맷팅
     */
    private String formatItemNames(String itemNames) {
        if (itemNames == null || itemNames.trim().isEmpty()) {
            return "";
        }
        
        String[] items = itemNames.split(", ");
        if (items.length <= 1) {
            return items[0];
        } else {
            return items[0] + " 외 " + (items.length - 1) + "개";
        }
    }
    
    /**
     * 엑셀 파일 생성 (템플릿 기반)
     */
    private byte[] createExcelFile(List<TaxInvoiceExcelRespDto> data) throws Exception {
        // 템플릿 엑셀 로드
        ClassPathResource templateResource = new ClassPathResource("templates/taxInvoice.xlsx");
        
        try (InputStream templateStream = templateResource.getInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 7번 행부터 데이터 입력 (6번 행은 제목)
            int rowIndex = 6; // 0-based로 6이면 실제 7번째 행
            
            for (TaxInvoiceExcelRespDto dto : data) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    row = sheet.createRow(rowIndex);
                }
                
                // 각 컬럼에 데이터 입력 (컬럼 순서는 템플릿에 따라 조정 필요)
                int colIndex = 0;
                setCellValue(row, colIndex++, dto.getInvoiceType());          // A: 전자세금계산서종류
                setCellValue(row, colIndex++, dto.getIssueDate());            // B: 작성일자
                setCellValue(row, colIndex++, dto.getHqBizNum().replace("-", ""));             // C: 공급자등록번호(본사)
                setCellValue(row, colIndex++, dto.getSupplierBranchNum());    // D: 공급자종사업장번호
                setCellValue(row, colIndex++, dto.getSupplierCompanyName());  // E: 공급자상호
                setCellValue(row, colIndex++, dto.getSupplierCeoName());      // F: 공급자성명
                setCellValue(row, colIndex++, dto.getSupplierAddr());         // G: 공급자사업장주소
                setCellValue(row, colIndex++, dto.getSupplierBizType());      // H: 공급자업태
                setCellValue(row, colIndex++, dto.getSupplierBizSector());    // I: 공급자업종
                setCellValue(row, colIndex++, null);        // J: 공급자이메일
                setCellValue(row, colIndex++, dto.getCustomerBizNum().replace("-", ""));       // K: 공급받는자등록번호
                setCellValue(row, colIndex++, "");
                setCellValue(row, colIndex++, dto.getCustomerName());       // K: 공급받는자상호
                setCellValue(row, colIndex++, dto.getCustomerOwnerName());       // K: 공급받는자성명
                setCellValue(row, colIndex++, dto.getCustomerAddr());       // K: 공급받는자주소
                setCellValue(row, colIndex++, dto.getCustomerBizType());      // L: 공급받는자업태
                setCellValue(row, colIndex++, dto.getCustomerBizSector());    // M: 공급받는자업종
                setCellValue(row, colIndex++, dto.getCustomerEmail());        // N: 공급받는자이메일1
                setCellValue(row, colIndex++, "");
                setCellValue(row, colIndex++, dto.getSupplyAmount());         // O: 공급가액
                setCellValue(row, colIndex++, "");
                setCellValue(row, colIndex++, dto.getDayOnly());              // P: 일자
                setCellValue(row, colIndex++, dto.getItemNames());            // Q: 품목1
                setCellValue(row, colIndex++, "EA");
                setCellValue(row, colIndex++, "1");
                setCellValue(row, colIndex++, dto.getSupplyAmount());         // O: 공급가액
                setCellValue(row, colIndex++, dto.getSupplyAmount());         // O: 공급가액
                setCellValue(row, colIndex++, "");
                setCellValue(row, colIndex++, dto.getReceiptType());          // R: 영수
                
                rowIndex++;
            }
            
            // 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 셀 값 설정 유틸리티
     */
    private void setCellValue(Row row, int columnIndex, Object value) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex);
        }
        
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Integer) {
            cell.setCellValue(((Integer) value).doubleValue());
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
}