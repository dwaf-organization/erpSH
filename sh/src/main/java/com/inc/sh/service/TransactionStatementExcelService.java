package com.inc.sh.service;

import com.inc.sh.dto.transactionStatement.reqDto.TransactionStatementExcelReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionStatementExcelService {
    
    private final OrderRepository orderRepository;
    
    /**
     * 거래명세표 엑셀 생성 (주문번호별 분리)
     */
    @Transactional(readOnly = true)
    public RespDto<Map<String, byte[]>> generateTransactionStatementExcel(TransactionStatementExcelReqDto reqDto) {
        try {
            log.info("거래명세표 엑셀 생성 시작 - 주문 개수: {}", reqDto.getOrderNumbers().size());
            
            // 주문 기본 정보 조회
            List<Object[]> orderInfoResults = orderRepository.findOrderInfoForTransactionStatement(reqDto.getOrderNumbers());
            
            // 주문 상세 항목 조회
            List<Object[]> orderItemResults = orderRepository.findOrderItemsForTransactionStatement(reqDto.getOrderNumbers());
            
            // 수금 정보 조회
            List<Object[]> collectionResults = orderRepository.findCollectionInfoForTransactionStatement(reqDto.getOrderNumbers());
            
            // 주문번호별로 그룹핑
            Map<String, Object[]> orderInfoMap = orderInfoResults.stream()
                    .collect(Collectors.toMap(row -> (String) row[0], row -> row, (a, b) -> a));
            
            Map<String, List<Object[]>> orderItemsMap = orderItemResults.stream()
                    .collect(Collectors.groupingBy(row -> (String) row[0]));
            
            Map<String, Object[]> collectionMap = collectionResults.stream()
                    .collect(Collectors.toMap(row -> (String) row[0], row -> row, (a, b) -> a));
            
            // 주문별로 엑셀 생성
            Map<String, byte[]> excelFiles = new LinkedHashMap<>();
            
            for (String orderNo : reqDto.getOrderNumbers()) {
                try {
                    Object[] orderInfo = orderInfoMap.get(orderNo);
                    List<Object[]> orderItems = orderItemsMap.get(orderNo);
                    Object[] collectionInfo = collectionMap.get(orderNo);
                    
                    if (orderInfo == null) {
                        log.warn("주문 정보를 찾을 수 없습니다 - orderNo: {}", orderNo);
                        continue;
                    }
                    
                    byte[] excelData = createSingleOrderExcel(orderNo, orderInfo, orderItems, collectionInfo);
                    excelFiles.put(orderNo, excelData);
                    
                    log.info("거래명세표 엑셀 생성 완료 - orderNo: {}", orderNo);
                    
                } catch (Exception e) {
                    log.error("주문별 엑셀 생성 실패 - orderNo: {}", orderNo, e);
                }
            }
            
            log.info("거래명세표 엑셀 생성 전체 완료 - 생성된 파일 수: {}", excelFiles.size());
            
            return RespDto.success("거래명세표 엑셀 생성 완료", excelFiles);
            
        } catch (Exception e) {
            log.error("거래명세표 엑셀 생성 중 오류 발생", e);
            return RespDto.fail("거래명세표 엑셀 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 단일 주문 엑셀 생성
     */
    private byte[] createSingleOrderExcel(String orderNo, Object[] orderInfo, 
                                         List<Object[]> orderItems, Object[] collectionInfo) throws Exception {
        
        log.info("단일 주문 엑셀 생성 시작 - orderNo: {}", orderNo);
        
        // 템플릿 엑셀 로드
        ClassPathResource templateResource = new ClassPathResource("templates/transaction_statement_template.xlsx");
        
        try (InputStream templateStream = templateResource.getInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 기본 정보 입력
            fillBasicInfo(sheet, orderNo, orderInfo);
            
            // 공급자 정보 입력 (Headquarter)
            fillSupplierInfo(sheet, orderInfo);
            
            // 공급받는자 정보 입력 (Customer)  
            fillCustomerInfo(sheet, orderInfo);
            
            // 결제계좌와 전체공지 입력
            fillPaymentAccountAndNotice(sheet, orderInfo);
            
            // 주문 상세 항목 입력
            fillOrderItems(sheet, orderItems);
            
            // 합계 및 수금 정보 입력
            fillTotalAndCollection(sheet, orderInfo, collectionInfo);
            
            // 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("단일 주문 엑셀 생성 완료 - orderNo: {}, 파일 크기: {} bytes", 
                    orderNo, outputStream.size());
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 기본 정보 입력
     */
    private void fillBasicInfo(Sheet sheet, String orderNo, Object[] orderInfo) {
        String deliveryRequestDt = (String) orderInfo[1];
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 1번 블록
        setCellValue(sheet, 9, 3, "1");                     // J3: 페이지번호
        setCellValue(sheet, 9, 4, orderNo);                 // J4: 주문번호
        setCellValue(sheet, 9, 5, today);                   // J5: 출력일자
        setCellValue(sheet, 3, 11, formatDate(deliveryRequestDt)); // C11: 납기요청
        setCellValue(sheet, 3, 12, null);                   // C12: 비고 (null)
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 9, 33, "1");                    // J33: 페이지번호
        setCellValue(sheet, 9, 34, orderNo);                // J34: 주문번호
        setCellValue(sheet, 9, 35, today);                  // J35: 출력일자
        setCellValue(sheet, 3, 41, formatDate(deliveryRequestDt)); // C41: 납기요청
        setCellValue(sheet, 3, 42, null);                   // C42: 비고
    }
    
    /**
     * 공급자 정보 입력 (Headquarter)
     */
    private void fillSupplierInfo(Sheet sheet, Object[] orderInfo) {
        String hqBizNum = (String) orderInfo[14];           // h.biz_num
        String companyName = (String) orderInfo[13];        // h.company_name  
        String ceoName = (String) orderInfo[15];            // h.ceo_name
        String hqBizType = (String) orderInfo[16];          // h.biz_type
        String bizItem = (String) orderInfo[17];            // h.biz_item
        String hqAddr = (String) orderInfo[18];             // h.addr
        String hqTel = (String) orderInfo[19];              // h.tel_num
        
        // 1번 블록
        setCellValue(sheet, 3, 6, hqBizNum);                // C6: 등록번호
        setCellValue(sheet, 3, 7, companyName);             // C7: 상호
        setCellValue(sheet, 3, 8, ceoName);                 // C8: 대표자
        setCellValue(sheet, 3, 9, hqBizType);               // C9: 업태
        setCellValue(sheet, 3, 10, hqAddr);                 // C10: 주소
        setCellValue(sheet, 5, 8, hqTel);                   // E8: Tel
        setCellValue(sheet, 5, 9, bizItem);                 // E9: 업종
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 3, 36, hqBizNum);               // C36: 등록번호
        setCellValue(sheet, 3, 37, companyName);            // C37: 상호
        setCellValue(sheet, 3, 38, ceoName);                // C38: 대표자
        setCellValue(sheet, 3, 39, hqBizType);              // C39: 업태
        setCellValue(sheet, 3, 40, hqAddr);                 // C40: 주소
        setCellValue(sheet, 5, 38, hqTel);                  // E38: Tel
        setCellValue(sheet, 5, 39, bizItem);                // E39: 업종
    }
    
    /**
     * 공급받는자 정보 입력 (Customer)
     */
    private void fillCustomerInfo(Sheet sheet, Object[] orderInfo) {
        String customerBizNum = (String) orderInfo[7];      // c.biz_num
        String customerName = (String) orderInfo[6];        // c.customer_name
        String ownerName = (String) orderInfo[8];           // c.owner_name
        String customerBizType = (String) orderInfo[9];     // c.biz_type
        String bizSector = (String) orderInfo[10];          // c.biz_sector
        String customerAddr = (String) orderInfo[11];       // c.addr
        String customerTel = (String) orderInfo[12];        // c.tel_num
        
        // 1번 블록
        setCellValue(sheet, 8, 6, customerBizNum);          // H6: 등록번호
        setCellValue(sheet, 8, 7, customerName);            // H7: 상호
        setCellValue(sheet, 8, 8, ownerName);               // H8: 대표자
        setCellValue(sheet, 8, 9, customerBizType);         // H9: 업태
        setCellValue(sheet, 8, 10, customerAddr);           // H10: 주소
        setCellValue(sheet, 11, 8, customerTel);            // K8: Tel
        setCellValue(sheet, 11, 9, bizSector);              // K9: 업종
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 8, 36, customerBizNum);         // H36: 등록번호
        setCellValue(sheet, 8, 37, customerName);           // H37: 상호
        setCellValue(sheet, 8, 38, ownerName);              // H38: 대표자
        setCellValue(sheet, 8, 39, customerBizType);        // H39: 업태
        setCellValue(sheet, 8, 40, customerAddr);           // H40: 주소
        setCellValue(sheet, 11, 38, customerTel);           // K38: Tel
        setCellValue(sheet, 11, 39, bizSector);             // K39: 업종
    }
    
    /**
     * 결제계좌와 전체공지 입력
     */
    private void fillPaymentAccountAndNotice(Sheet sheet, Object[] orderInfo) {
        String bankName = (String) orderInfo[20];           // va.bank_name
        String accountNum = (String) orderInfo[21];         // va.virtual_account_num
        
        String paymentAccount = "";
        if (bankName != null && accountNum != null) {
            paymentAccount = bankName + " " + accountNum;
        }
        
        // 1번 블록
        setCellValue(sheet, 3, 3, paymentAccount);          // C3: 결제계좌
        setCellValue(sheet, 3, 4, null);                    // C4: 전체공지 (null)
        
        // 2번 블록
        setCellValue(sheet, 3, 33, paymentAccount);         // C33: 결제계좌
        setCellValue(sheet, 3, 34, null);                   // C34: 전체공지 (null)
    }
    
    /**
     * 주문 상세 항목 입력 (페이지 넘어가도록 처리)
     */
    private void fillOrderItems(Sheet sheet, List<Object[]> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;
        
        int maxItemsPerBlock = 10; // 한 블록당 최대 항목 수
        int currentItemIndex = 0;
        
        for (Object[] item : orderItems) {
            // 현재 아이템이 몇 번째 블록에 들어가야 하는지 계산
            int blockNumber = currentItemIndex / maxItemsPerBlock;
            int itemIndexInBlock = currentItemIndex % maxItemsPerBlock;
            
            if (blockNumber == 0) {
                // 1번 블록 (14-23행)
                int rowNum = 14 + itemIndexInBlock;
                fillSingleOrderItem(sheet, rowNum, currentItemIndex + 1, item);
            } else if (blockNumber == 1) {
                // 2번 블록 (44-53행)
                int rowNum = 44 + itemIndexInBlock;
                fillSingleOrderItem(sheet, rowNum, currentItemIndex + 1, item);
            } else {
                // 20개 이상인 경우 새 시트나 블록 추가 필요
                // 우선은 로그만 남기고 스킵
                log.warn("주문 항목이 20개를 초과했습니다. 항목 인덱스: {}, 추가 처리 필요", currentItemIndex);
            }
            
            currentItemIndex++;
        }
        
        log.info("주문 항목 입력 완료 - 총 항목 수: {}", orderItems.size());
    }
    
    /**
     * 단일 주문 항목 입력 - BigDecimal 캐스팅 오류 수정
     */
    private void fillSingleOrderItem(Sheet sheet, int rowNum, int seqNo, Object[] item) {
        // item 배열: [order_no, item_name, specification, unit, order_qty, order_unit_price, supply_amt, vat_amt, total_amt]
        String itemName = (String) item[1];         // item_name
        String specification = (String) item[2];    // specification
        String unit = (String) item[3];             // unit
        Integer orderQty = ((Number) item[4]).intValue();       // order_qty
        Integer unitPrice = ((Number) item[5]).intValue();      // order_unit_price
        Integer supplyAmt = ((Number) item[6]).intValue();      // supply_amt
        Integer vatAmt = ((Number) item[7]).intValue();         // vat_amt
        Integer totalAmt = ((Number) item[8]).intValue();       // total_amt
        
        setCellValue(sheet, 1, rowNum, seqNo);                      // A: 순번
        setCellValue(sheet, 2, rowNum, "과세");                     // B: 면/과세
        setCellValue(sheet, 3, rowNum, itemName);                   // C: 품명
        setCellValue(sheet, 4, rowNum, specification);              // D: 규격
        setCellValue(sheet, 5, rowNum, unit);                       // E: 단위
        setCellValue(sheet, 6, rowNum, orderQty);                   // F: 수량
        setCellValue(sheet, 7, rowNum, unitPrice);                  // G: 단가
        setCellValue(sheet, 8, rowNum, supplyAmt);                  // H: 공급가
        setCellValue(sheet, 9, rowNum, vatAmt);                     // I: 부가세
        setCellValue(sheet, 10, rowNum, totalAmt);                  // J: 합계금액
        setCellValue(sheet, 11, rowNum, "");                        // K: 비고
    }
    
    /**
     * 합계 및 수금 정보 입력 - BigDecimal 캐스팅 오류 수정
     */
    private void fillTotalAndCollection(Sheet sheet, Object[] orderInfo, Object[] collectionInfo) {
        Integer totalAmt = ((Number) orderInfo[2]).intValue();          // o.total_amt
        Integer supplyAmt = ((Number) orderInfo[3]).intValue();         // o.supply_amt
        Integer vatAmt = ((Number) orderInfo[4]).intValue();            // o.vat_amt
        Integer depositTypeCode = ((Number) orderInfo[5]).intValue();   // o.deposit_type_code
        
        // 수금 정보 계산
        Integer depositAmount = 0;
        if (collectionInfo != null) {
            depositAmount = ((Number) collectionInfo[3]).intValue();     // deposit_amount
        }
        
        // 당일수금/미수잔액 계산
        Integer todayCollection;
        Integer uncollectedBalance;
        
        if (depositTypeCode == 1) { // 충전형
            todayCollection = totalAmt;
            uncollectedBalance = 0;
        } else { // 후입금
            todayCollection = depositAmount;
            uncollectedBalance = totalAmt - depositAmount;
        }
        
        // 1번 블록 합계
        setCellValue(sheet, 6, 24, null);                   // F24: 수량 합계 (필요시)
        setCellValue(sheet, 7, 24, null);                   // G24: 단가 합계 (필요시) 
        setCellValue(sheet, 8, 24, supplyAmt);              // H24: 공급가 합계
        setCellValue(sheet, 9, 24, vatAmt);                 // I24: 부가세 합계
        setCellValue(sheet, 10, 24, totalAmt);              // J24: 합계금액 합계
        
        setCellValue(sheet, 3, 26, totalAmt);               // C26: 주문합계
        setCellValue(sheet, 5, 26, todayCollection);        // E26: 당일수급
        setCellValue(sheet, 7, 26, uncollectedBalance);     // G26: 미수금
        
        // 2번 블록 합계
        setCellValue(sheet, 6, 54, null);                   // F54: 수량 합계
        setCellValue(sheet, 7, 54, null);                   // G54: 단가 합계
        setCellValue(sheet, 8, 54, supplyAmt);              // H54: 공급가 합계
        setCellValue(sheet, 9, 54, vatAmt);                 // I54: 부가세 합계
        setCellValue(sheet, 10, 54, totalAmt);              // J54: 합계금액 합계
        
        setCellValue(sheet, 3, 56, totalAmt);               // C56: 주문합계
        setCellValue(sheet, 5, 56, todayCollection);        // E56: 당일수급
        setCellValue(sheet, 7, 56, uncollectedBalance);     // G56: 미수금
    }
    
    /**
     * 셀 값 설정 유틸리티
     */
    private void setCellValue(Sheet sheet, int colIndex, int rowIndex, Object value) {
        Row row = sheet.getRow(rowIndex - 1); // 0-based index
        if (row == null) {
            row = sheet.createRow(rowIndex - 1);
        }
        
        Cell cell = row.getCell(colIndex - 1); // 0-based index
        if (cell == null) {
            cell = row.createCell(colIndex - 1);
        }
        
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    /**
     * 날짜 포맷 변환 (YYYYMMDD → YYYY-MM-DD)
     */
    private String formatDate(String dateStr) {
        try {
            if (dateStr != null && dateStr.length() == 8) {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return dateStr;
        } catch (Exception e) {
            return dateStr;
        }
    }
}