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
     * 거래명세표 엑셀 생성 (주문번호별 분리, 하나의 시트에 세로로 길게)
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
     * 단일 주문 엑셀 생성 (하나의 시트에 세로로 길게)
     */
    private byte[] createSingleOrderExcel(String orderNo, Object[] orderInfo, 
                                         List<Object[]> orderItems, Object[] collectionInfo) throws Exception {
        
        log.info("단일 주문 엑셀 생성 시작 - orderNo: {}, 총 품목: {}개", orderNo, orderItems != null ? orderItems.size() : 0);
        
        // 템플릿 엑셀 로드
        ClassPathResource templateResource = new ClassPathResource("templates/transaction_statement_template.xlsx");
        
        try (InputStream templateStream = templateResource.getInputStream();
             Workbook workbook = new XSSFWorkbook(templateStream)) {
            
            Sheet sheet = workbook.getSheetAt(0);
            
            // 품목을 10개씩 그룹으로 나누기
            List<List<Object[]>> itemGroups = groupItemsByPage(orderItems, 10);
            int totalPages = itemGroups.size();
            
            log.info("페이지 수: {} (품목 {}개)", totalPages, orderItems != null ? orderItems.size() : 0);
            
            // 각 페이지별로 처리
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {
                List<Object[]> pageItems = itemGroups.get(pageIndex);
                int currentPage = pageIndex + 1;
                int startRow = pageIndex * 58; // 각 페이지는 57행 간격
                
                log.info("페이지 {} 생성 시작 - 시작행: {}, 품목: {}개", currentPage, startRow + 1, pageItems.size());
                
                if (pageIndex > 0) {
                    // 2페이지부터는 템플릿 구조를 아래쪽에 복사
                    copyTemplateStructureDown(sheet, startRow);
                }
                
                // 페이지별 데이터 입력
                fillPageDataAtRow(sheet, orderNo, orderInfo, pageItems, collectionInfo, 
                                orderItems, currentPage, totalPages, startRow);
                
                // 페이지 나누기 추가 (마지막 페이지 제외)
                if (pageIndex < totalPages - 1) {
                    int pageBreakRow = startRow + 58; // 57행 뒤에 페이지 나누기
                    sheet.setRowBreak(pageBreakRow);
                    log.info("페이지 나누기 추가: {}행 뒤", pageBreakRow + 1);
                }
                
                log.info("페이지 {} 생성 완료 - 행 범위: {}-{}", currentPage, startRow + 1, startRow + 58);
            }
            
            // 바이트 배열로 변환
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("단일 주문 엑셀 생성 완료 - orderNo: {}, 페이지 수: {}, 총 행 수: {}, 파일 크기: {} bytes", 
                    orderNo, totalPages, totalPages * 58, outputStream.size());
            
            return outputStream.toByteArray();
        }
    }
    
    /**
     * 품목을 페이지별로 그룹핑 (10개씩)
     */
    private List<List<Object[]>> groupItemsByPage(List<Object[]> orderItems, int itemsPerPage) {
        List<List<Object[]>> groups = new ArrayList<>();
        
        if (orderItems == null || orderItems.isEmpty()) {
            // 빈 그룹 하나라도 만들어야 페이지가 생성됨
            groups.add(new ArrayList<>());
            return groups;
        }
        
        for (int i = 0; i < orderItems.size(); i += itemsPerPage) {
            int endIndex = Math.min(i + itemsPerPage, orderItems.size());
            List<Object[]> group = orderItems.subList(i, endIndex);
            groups.add(new ArrayList<>(group));
        }
        
        return groups;
    }
    
    /**
     * 템플릿 구조를 아래쪽 행에 복사 (병합 영역 포함)
     */
    private void copyTemplateStructureDown(Sheet sheet, int startRow) {
        // 원본 템플릿 범위: 0-56행 (57행)
        for (int rowIdx = 0; rowIdx < 58; rowIdx++) {
            Row sourceRow = sheet.getRow(rowIdx);
            if (sourceRow != null) {
                Row targetRow = sheet.getRow(startRow + rowIdx);
                if (targetRow == null) {
                    targetRow = sheet.createRow(startRow + rowIdx);
                }
                
                // 행 높이 복사
                targetRow.setHeight(sourceRow.getHeight());
                
                // 각 셀 복사
                for (int cellIdx = 0; cellIdx < sourceRow.getLastCellNum(); cellIdx++) {
                    Cell sourceCell = sourceRow.getCell(cellIdx);
                    if (sourceCell != null) {
                        Cell targetCell = targetRow.getCell(cellIdx);
                        if (targetCell == null) {
                            targetCell = targetRow.createCell(cellIdx);
                        }
                        
                        // 스타일 복사 (같은 워크북이므로 안전함)
                        targetCell.setCellStyle(sourceCell.getCellStyle());
                        
                        // 값 복사 (템플릿 기본값만)
                        switch (sourceCell.getCellType()) {
                            case STRING:
                                targetCell.setCellValue(sourceCell.getStringCellValue());
                                break;
                            case NUMERIC:
                                targetCell.setCellValue(sourceCell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                targetCell.setCellValue(sourceCell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                try {
                                    if (sourceCell.getCachedFormulaResultType() == CellType.NUMERIC) {
                                        targetCell.setCellValue(sourceCell.getNumericCellValue());
                                    } else if (sourceCell.getCachedFormulaResultType() == CellType.STRING) {
                                        targetCell.setCellValue(sourceCell.getStringCellValue());
                                    }
                                } catch (Exception e) {
                                    targetCell.setCellValue(""); // 오류 시 빈 값
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }
        
        // 병합된 셀 영역 복사 (템플릿 범위 0-56행)
        copyMergedRegions(sheet, 0, 56, startRow);
        
        log.debug("템플릿 구조 복사 완료 - 시작행: {}, 병합 영역 포함", startRow + 1);
    }
    
    /**
     * 안전한 병합된 셀 영역 복사
     */
    private void copyMergedRegions(Sheet sheet, int sourceStartRow, int sourceEndRow, int targetStartRow) {
        try {
            List<org.apache.poi.ss.util.CellRangeAddress> mergedRegions = new ArrayList<>(sheet.getMergedRegions());
            
            for (org.apache.poi.ss.util.CellRangeAddress mergedRegion : mergedRegions) {
                // 소스 범위 내의 병합 영역만 복사
                if (mergedRegion.getFirstRow() >= sourceStartRow && mergedRegion.getLastRow() <= sourceEndRow) {
                    
                    // 새로운 위치로 병합 영역 이동
                    int newFirstRow = mergedRegion.getFirstRow() - sourceStartRow + targetStartRow;
                    int newLastRow = mergedRegion.getLastRow() - sourceStartRow + targetStartRow;
                    int newFirstCol = mergedRegion.getFirstColumn();
                    int newLastCol = mergedRegion.getLastColumn();
                    
                    // 유효한 범위인지 체크
                    if (newFirstRow >= 0 && newLastRow < 1048576 && newFirstCol >= 0 && newLastCol < 16384) {
                        org.apache.poi.ss.util.CellRangeAddress newMergedRegion = 
                            new org.apache.poi.ss.util.CellRangeAddress(newFirstRow, newLastRow, newFirstCol, newLastCol);
                        
                        // 안전하게 추가
                        try {
                            sheet.addMergedRegion(newMergedRegion);
                            log.debug("병합 영역 복사 성공: ({},{}) to ({},{})", 
                                     newFirstRow + 1, newFirstCol + 1, newLastRow + 1, newLastCol + 1);
                        } catch (Exception e) {
                            log.warn("병합 영역 복사 실패: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("병합 영역 복사 중 오류 발생", e);
        }
    }
    
    /**
     * 중복 병합 영역 체크
     */
    private boolean isDuplicateMergedRegion(Sheet sheet, org.apache.poi.ss.util.CellRangeAddress newRegion) {
        List<org.apache.poi.ss.util.CellRangeAddress> existingRegions = sheet.getMergedRegions();
        
        for (org.apache.poi.ss.util.CellRangeAddress existing : existingRegions) {
            if (existing.getFirstRow() == newRegion.getFirstRow() &&
                existing.getLastRow() == newRegion.getLastRow() &&
                existing.getFirstColumn() == newRegion.getFirstColumn() &&
                existing.getLastColumn() == newRegion.getLastColumn()) {
                return true; // 중복됨
            }
        }
        return false; // 중복 아님
    }
    
    /**
     * 특정 행 위치에서 페이지별 데이터 입력
     */
    private void fillPageDataAtRow(Sheet sheet, String orderNo, Object[] orderInfo, 
                                  List<Object[]> pageItems, Object[] collectionInfo, 
                                  List<Object[]> allItems, int currentPage, int totalPages, int startRow) {
        
        // 기본 정보 입력 (페이지 번호 포함)
        fillBasicInfoWithPageAtRow(sheet, orderNo, orderInfo, currentPage, totalPages, startRow);
        
        // 공급자 정보 입력
        fillSupplierInfoAtRow(sheet, orderInfo, startRow);
        
        // 공급받는자 정보 입력
        fillCustomerInfoAtRow(sheet, orderInfo, startRow);
        
        // 결제계좌와 전체공지 입력
        fillPaymentAccountAndNoticeAtRow(sheet, orderInfo, startRow);
        
        // 페이지별 품목 입력 (1번 블록 + 2번 블록 복사)
        fillOrderItemsForPageAtRow(sheet, pageItems, (currentPage - 1) * 10, startRow);
        
        // 전체 품목 기준 합계 입력 (모든 페이지에서 동일)
        fillTotalAndCollectionAtRow(sheet, orderInfo, collectionInfo, startRow);
    }
    
    /**
     * 특정 행 위치에서 기본 정보 입력 (페이지 번호 포함)
     */
    private void fillBasicInfoWithPageAtRow(Sheet sheet, String orderNo, Object[] orderInfo, 
                                           int currentPage, int totalPages, int startRow) {
        String deliveryRequestDt = (String) orderInfo[1];
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String pageInfo = currentPage + "/" + totalPages;
        
        // 1번 블록
        setCellValue(sheet, 10, startRow + 3, pageInfo);                // J3: 페이지번호 (1/2)
        setCellValue(sheet, 10, startRow + 4, orderNo);                 // J4: 주문번호
        setCellValue(sheet, 10, startRow + 5, today);                   // J5: 출력일자
        setCellValue(sheet, 3, startRow + 11, formatDate(deliveryRequestDt)); // C11: 납기요청
        setCellValue(sheet, 3, startRow + 12, null);                   // C12: 비고 (null)
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 10, startRow + 33, pageInfo);               // J33: 페이지번호 (1/2)
        setCellValue(sheet, 10, startRow + 34, orderNo);                // J34: 주문번호
        setCellValue(sheet, 10, startRow + 35, today);                  // J35: 출력일자
        setCellValue(sheet, 3, startRow + 41, formatDate(deliveryRequestDt)); // C41: 납기요청
        setCellValue(sheet, 3, startRow + 42, null);                   // C42: 비고
    }
    
    /**
     * 특정 행 위치에서 공급자 정보 입력
     */
    private void fillSupplierInfoAtRow(Sheet sheet, Object[] orderInfo, int startRow) {
        String hqBizNum = (String) orderInfo[14];           // h.biz_num
        String companyName = (String) orderInfo[13];        // h.company_name  
        String ceoName = (String) orderInfo[15];            // h.ceo_name
        String hqBizType = (String) orderInfo[16];          // h.biz_type
        String bizItem = (String) orderInfo[17];            // h.biz_item
        String hqAddr = (String) orderInfo[18];             // h.addr
        String hqTel = (String) orderInfo[19];              // h.tel_num
        
        // 1번 블록
        setCellValue(sheet, 3, startRow + 6, hqBizNum);                // C6: 등록번호
        setCellValue(sheet, 3, startRow + 7, companyName);             // C7: 상호
        setCellValue(sheet, 3, startRow + 8, ceoName);                 // C8: 대표자
        setCellValue(sheet, 3, startRow + 9, hqBizType);               // C9: 업태
        setCellValue(sheet, 3, startRow + 10, hqAddr);                 // C10: 주소
        setCellValue(sheet, 5, startRow + 8, hqTel);                   // E8: Tel
        setCellValue(sheet, 5, startRow + 9, bizItem);                 // E9: 업종
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 3, startRow + 36, hqBizNum);               // C36: 등록번호
        setCellValue(sheet, 3, startRow + 37, companyName);            // C37: 상호
        setCellValue(sheet, 3, startRow + 38, ceoName);                // C38: 대표자
        setCellValue(sheet, 3, startRow + 39, hqBizType);              // C39: 업태
        setCellValue(sheet, 3, startRow + 40, hqAddr);                 // C40: 주소
        setCellValue(sheet, 5, startRow + 38, hqTel);                  // E38: Tel
        setCellValue(sheet, 5, startRow + 39, bizItem);                // E39: 업종
    }
    
    /**
     * 특정 행 위치에서 공급받는자 정보 입력
     */
    private void fillCustomerInfoAtRow(Sheet sheet, Object[] orderInfo, int startRow) {
        String customerBizNum = (String) orderInfo[7];      // c.biz_num
        String customerName = (String) orderInfo[6];        // c.customer_name
        String ownerName = (String) orderInfo[8];           // c.owner_name
        String customerBizType = (String) orderInfo[9];     // c.biz_type
        String bizSector = (String) orderInfo[10];          // c.biz_sector
        String customerAddr = (String) orderInfo[11];       // c.addr
        String customerTel = (String) orderInfo[12];        // c.tel_num
        
        // 1번 블록
        setCellValue(sheet, 8, startRow + 6, customerBizNum);          // H6: 등록번호
        setCellValue(sheet, 8, startRow + 7, customerName);            // H7: 상호
        setCellValue(sheet, 8, startRow + 8, ownerName);               // H8: 대표자
        setCellValue(sheet, 8, startRow + 9, customerBizType);         // H9: 업태
        setCellValue(sheet, 8, startRow + 10, customerAddr);           // H10: 주소
        setCellValue(sheet, 11, startRow + 8, customerTel);            // K8: Tel
        setCellValue(sheet, 11, startRow + 9, bizSector);              // K9: 업종
        
        // 2번 블록 (30행 추가)
        setCellValue(sheet, 8, startRow + 36, customerBizNum);         // H36: 등록번호
        setCellValue(sheet, 8, startRow + 37, customerName);           // H37: 상호
        setCellValue(sheet, 8, startRow + 38, ownerName);              // H38: 대표자
        setCellValue(sheet, 8, startRow + 39, customerBizType);        // H39: 업태
        setCellValue(sheet, 8, startRow + 40, customerAddr);           // H40: 주소
        setCellValue(sheet, 11, startRow + 38, customerTel);           // K38: Tel
        setCellValue(sheet, 11, startRow + 39, bizSector);             // K39: 업종
    }
    
    /**
     * 특정 행 위치에서 결제계좌와 전체공지 입력
     */
    private void fillPaymentAccountAndNoticeAtRow(Sheet sheet, Object[] orderInfo, int startRow) {
        String bankName = (String) orderInfo[20];           // va.bank_name
        String accountNum = (String) orderInfo[21];         // va.virtual_account_num
        
        String paymentAccount = "";
        if (bankName != null && accountNum != null) {
            paymentAccount = bankName + " " + accountNum;
        }
        
        // 1번 블록
        setCellValue(sheet, 3, startRow + 3, paymentAccount);          // C3: 결제계좌
        setCellValue(sheet, 3, startRow + 4, null);                    // C4: 전체공지 (null)
        
        // 2번 블록
        setCellValue(sheet, 3, startRow + 33, paymentAccount);         // C33: 결제계좌
        setCellValue(sheet, 3, startRow + 34, null);                   // C34: 전체공지 (null)
    }
    
    /**
     * 특정 행 위치에서 페이지별 주문 항목 입력 (1번 블록 + 2번 블록 복사)
     */
    private void fillOrderItemsForPageAtRow(Sheet sheet, List<Object[]> pageItems, int startSequence, int startRow) {
        if (pageItems == null || pageItems.isEmpty()) return;
        
        log.info("페이지별 품목 입력 시작 - 품목 수: {}, 시작 순번: {}, 시작행: {}", 
                pageItems.size(), startSequence + 1, startRow + 1);
        
        // 기존 품목 데이터 클리어
        clearItemRowsAtRow(sheet, startRow + 14, startRow + 23);  // 1번 블록 클리어
        clearItemRowsAtRow(sheet, startRow + 44, startRow + 53);  // 2번 블록 클리어
        
        // 1번 블록에 품목 입력 (14-23행)
        for (int i = 0; i < pageItems.size() && i < 10; i++) {
            Object[] item = pageItems.get(i);
            int rowNum = startRow + 14 + i;  // 시작행 + 14행부터
            int seqNo = startSequence + i + 1;  // 전체 순번 계산
            
            fillSingleOrderItemAtRow(sheet, rowNum, seqNo, item);
            log.debug("1번 블록 - 행: {}, 순번: {}, 품목: {}", rowNum + 1, seqNo, item[1]);
        }
        
        // 2번 블록에 1번 블록 복사 (44-53행)
        for (int i = 0; i < pageItems.size() && i < 10; i++) {
            Object[] item = pageItems.get(i);
            int rowNum = startRow + 44 + i;  // 시작행 + 44행부터
            int seqNo = startSequence + i + 1;  // 전체 순번 계산
            
            fillSingleOrderItemAtRow(sheet, rowNum, seqNo, item);
            log.debug("2번 블록 - 행: {}, 순번: {}, 품목: {}", rowNum + 1, seqNo, item[1]);
        }
        
        log.info("페이지별 품목 입력 완료 - 1번/2번 블록 모두 동일하게 입력됨");
    }
    
    /**
     * 특정 행 위치에서 품목 행 범위 클리어
     */
    private void clearItemRowsAtRow(Sheet sheet, int startRow, int endRow) {
        for (int rowNum = startRow; rowNum <= endRow; rowNum++) {
            Row row = sheet.getRow(rowNum); // 이미 0-based 처리됨
            if (row != null) {
                // A열부터 K열까지 클리어 (1~11)
                for (int colNum = 1; colNum <= 11; colNum++) {
                    Cell cell = row.getCell(colNum - 1);
                    if (cell != null) {
                        cell.setBlank();
                    }
                }
            }
        }
    }
    
    /**
     * 특정 행 위치에서 단일 주문 항목 입력
     */
    private void fillSingleOrderItemAtRow(Sheet sheet, int rowNum, int seqNo, Object[] item) {
        // item 배열: [order_no, item_name, specification, unit, order_qty, order_unit_price, supply_amt, vat_amt, total_amt]
        String itemName = (String) item[1];         // item_name
        String specification = (String) item[2];    // specification
        String unit = (String) item[3];             // unit
        Integer orderQty = ((Number) item[4]).intValue();       // order_qty
        Integer unitPrice = ((Number) item[5]).intValue();      // order_unit_price
        Integer supplyAmt = ((Number) item[6]).intValue();      // supply_amt
        Integer vatAmt = ((Number) item[7]).intValue();         // vat_amt
        Integer totalAmt = ((Number) item[8]).intValue();       // total_amt
        String taxTarget = (String) item[9];					// tax_target
        
        setCellValue(sheet, 1, rowNum, seqNo);                      // A: 순번
        setCellValue(sheet, 2, rowNum, taxTarget);                  // B: 면/과세
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
     * 특정 행 위치에서 합계 및 수금 정보 입력 (전체 품목 기준)
     */
    private void fillTotalAndCollectionAtRow(Sheet sheet, Object[] orderInfo, Object[] collectionInfo, int startRow) {
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
        setCellValue(sheet, 6, startRow + 24, null);                   // F24: 수량 합계 (필요시)
        setCellValue(sheet, 7, startRow + 24, null);                   // G24: 단가 합계 (필요시) 
        setCellValue(sheet, 8, startRow + 24, supplyAmt);              // H24: 공급가 합계
        setCellValue(sheet, 9, startRow + 24, vatAmt);                 // I24: 부가세 합계
        setCellValue(sheet, 10, startRow + 24, totalAmt);              // J24: 합계금액 합계
        
        setCellValue(sheet, 3, startRow + 26, totalAmt);               // C26: 주문합계
        setCellValue(sheet, 5, startRow + 26, todayCollection);        // E26: 당일수급
        setCellValue(sheet, 7, startRow + 26, uncollectedBalance);     // G26: 미수금
        
        // 2번 블록 합계
        setCellValue(sheet, 6, startRow + 54, null);                   // F54: 수량 합계
        setCellValue(sheet, 7, startRow + 54, null);                   // G54: 단가 합계
        setCellValue(sheet, 8, startRow + 54, supplyAmt);              // H54: 공급가 합계
        setCellValue(sheet, 9, startRow + 54, vatAmt);                 // I54: 부가세 합계
        setCellValue(sheet, 10, startRow + 54, totalAmt);              // J54: 합계금액 합계
        
        setCellValue(sheet, 3, startRow + 56, totalAmt);               // C56: 주문합계
        setCellValue(sheet, 5, startRow + 56, todayCollection);        // E56: 당일수급
        setCellValue(sheet, 7, startRow + 56, uncollectedBalance);     // G56: 미수금
    }
    
    /**
     * 셀 값 설정 유틸리티 (1-based 인덱스)
     */
    private void setCellValue(Sheet sheet, int colIndex, int rowIndex, Object value) {
        Row row = sheet.getRow(rowIndex - 1); // 0-based index로 변환
        if (row == null) {
            row = sheet.createRow(rowIndex - 1);
        }
        
        Cell cell = row.getCell(colIndex - 1); // 0-based index로 변환
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