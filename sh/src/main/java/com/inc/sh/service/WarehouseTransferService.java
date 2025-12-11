package com.inc.sh.service;

import com.inc.sh.dto.warehouseTransfer.reqDto.WarehouseTransferProcessDto;
import com.inc.sh.dto.warehouseTransfer.reqDto.WarehouseTransferListSearchDto;
import com.inc.sh.dto.warehouseTransfer.reqDto.TransferItemDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseItemRespDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseTransferListRespDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseTransferItemRespDto;
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
public class WarehouseTransferService {
    
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final WarehouseTransfersRepository warehouseTransfersRepository;
    private final WarehouseTransfersItemsRepository warehouseTransfersItemsRepository;
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    private final MonthlyClosingService monthlyClosingService;
    
    /**
     * 창고이송현황 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<WarehouseTransferListRespDto>> getWarehouseTransferList(WarehouseTransferListSearchDto searchDto) {
        try {
            log.info("창고이송현황 목록 조회 시작 - hqCode: {}, 기간: {}~{}, 출고창고: {}, 입고창고: {}", 
                    searchDto.getHqCode(), searchDto.getStartYm(), searchDto.getEndYm(), 
                    searchDto.getFromWarehouseCode(), searchDto.getToWarehouseCode());
            
            List<Object[]> results = warehouseTransfersRepository.findWarehouseTransferListWithHqCode(
                    searchDto.getStartYm(),
                    searchDto.getEndYm(),
                    searchDto.getFromWarehouseCode(),
                    searchDto.getToWarehouseCode(),
                    searchDto.getHqCode()
            );
            
            List<WarehouseTransferListRespDto> responseList = results.stream()
                    .map(result -> WarehouseTransferListRespDto.builder()
                            .transferCode((String) result[0])
                            .transferDate(formatTransferDate((String) result[1]))
                            .fromWarehouseCode((Integer) result[2])
                            .fromWarehouseName((String) result[3])
                            .toWarehouseCode((Integer) result[4])
                            .toWarehouseName((String) result[5])
                            .note((String) result[6])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("창고이송현황 목록 조회 완료 - hqCode: {}, 조회 건수: {}", searchDto.getHqCode(), responseList.size());
            return RespDto.success("창고이송현황 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("창고이송현황 목록 조회 중 오류 발생 - hqCode: {}", searchDto.getHqCode(), e);
            return RespDto.fail("창고이송현황 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고이송품목 상세 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<WarehouseTransferItemRespDto>> getWarehouseTransferItems(String transferCode) {
        try {
            log.info("창고이송품목 상세 조회 시작 - 이송번호: {}", transferCode);
            
            List<Object[]> results = warehouseTransfersItemsRepository.findWarehouseTransferItemsByTransferCode(transferCode);
            
            List<WarehouseTransferItemRespDto> responseList = results.stream()
                    .map(result -> WarehouseTransferItemRespDto.builder()
                            .itemCode((Integer) result[0])
                            .itemName((String) result[1])
                            .specification((String) result[2])
                            .unit((String) result[3])
                            .unitPrice(((Number) result[4]).intValue())
                            .quantity(((Number) result[5]).intValue())
                            .amount(((Number) result[6]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("창고이송품목 상세 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("창고이송품목 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("창고이송품목 상세 조회 중 오류 발생", e);
            return RespDto.fail("창고이송품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 이송일자 포맷 변환 (YYYYMMDD -> YYYY-MM-DD)
     */
    private String formatTransferDate(String transferDate) {
        try {
            if (transferDate != null && transferDate.length() == 8) {
                LocalDate date = LocalDate.parse(transferDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            return transferDate;
        } catch (Exception e) {
            log.warn("이송일자 포맷 변환 실패: {}", transferDate);
            return transferDate;
        }
    }
    
    /**
     * 출고창고 품목 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<WarehouseItemRespDto>> getWarehouseItems(Integer warehouseCode, Integer hqCode) {
        try {
            log.info("출고창고 품목 조회 시작 - 창고코드: {}, hqCode: {}", warehouseCode, hqCode);
            
            List<Object[]> results = warehouseItemsRepository.findWarehouseItemsForTransferWithHqCode(warehouseCode, hqCode);
            
            List<WarehouseItemRespDto> responseList = results.stream()
                    .map(result -> WarehouseItemRespDto.builder()
                            .itemCode((Integer) result[0])
                            .itemName((String) result[1])
                            .specification((String) result[2])
                            .unit((String) result[3])
                            .currentQuantity(((Number) result[4]).intValue())
                            .unitPrice(((Number) result[5]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("출고창고 품목 조회 완료 - hqCode: {}, 조회 건수: {}", hqCode, responseList.size());
            return RespDto.success("출고창고 품목 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("출고창고 품목 조회 중 오류 발생 - hqCode: {}", hqCode, e);
            return RespDto.fail("출고창고 품목 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 창고이송 처리
     */
    @Transactional
    public RespDto<String> processWarehouseTransfer(WarehouseTransferProcessDto processDto) {
        try {
            log.info("창고이송 처리 시작 - 출고창고: {}, 입고창고: {}, 품목수: {}", 
                    processDto.getFromWarehouseCode(), processDto.getToWarehouseCode(),
                    processDto.getItems() != null ? processDto.getItems().size() : 0);
            
            // 1. 마감 상태 체크
            String yearMonth = processDto.getTransferDate().substring(0, 6); // YYYYMM
            String fromClosingMessage = monthlyClosingService.getClosingCheckMessage(
                    yearMonth, processDto.getFromWarehouseCode());
            String toClosingMessage = monthlyClosingService.getClosingCheckMessage(
                    yearMonth, processDto.getToWarehouseCode());
            
            if (fromClosingMessage != null) {
                return RespDto.fail("출고창고가 " + fromClosingMessage);
            }
            if (toClosingMessage != null) {
                return RespDto.fail("입고창고가 " + toClosingMessage);
            }
            
            // 2. 재고 수량 체크
            for (TransferItemDto item : processDto.getItems()) {
                Optional<WarehouseItems> fromWarehouseItem = warehouseItemsRepository
                        .findByWarehouseCodeAndItemCode(processDto.getFromWarehouseCode(), item.getItemCode());
                
                if (fromWarehouseItem.isEmpty() || fromWarehouseItem.get().getCurrentQuantity() < item.getQuantity()) {
                    return RespDto.fail("출고창고의 재고가 부족합니다.");
                }
            }
            
            // 3. 이송번호 생성
            String transferCode = generateTransferCode(processDto.getTransferDate());
            
            // 4. 창고이송 기본정보 저장
            WarehouseTransfers transfer = WarehouseTransfers.builder()
                    .transferCode(transferCode)
                    .transferDate(processDto.getTransferDate())
                    .fromWarehouseCode(processDto.getFromWarehouseCode())
                    .toWarehouseCode(processDto.getToWarehouseCode())
                    .note(processDto.getNote())
                    .description("창고이송")
                    .build();
            
            warehouseTransfersRepository.save(transfer);
            
            // 5. 품목별 이송 처리
            for (TransferItemDto item : processDto.getItems()) {
                // 5-1. 이송품목 저장
                WarehouseTransfersItems transferItem = WarehouseTransfersItems.builder()
                        .transferCode(transferCode)
                        .itemCode(item.getItemCode())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .amount(item.getAmount())
                        .description("창고이송품목")
                        .build();
                
                warehouseTransfersItemsRepository.save(transferItem);
                
                // 5-2. 출고창고 재고 차감
                updateFromWarehouseQuantity(processDto.getFromWarehouseCode(), item.getItemCode(), 
                        item.getQuantity());
                
                // 5-3. 입고창고 재고 증가
                updateToWarehouseQuantity(processDto.getToWarehouseCode(), item.getItemCode(), 
                        item.getQuantity());
                
                // 5-4. 월별재고마감 업데이트
                updateMonthlyInventoryClosing(processDto.getFromWarehouseCode(), processDto.getToWarehouseCode(),
                        item.getItemCode(), item.getQuantity(), item.getAmount(), yearMonth);
                
                // 5-5. 재고수불부 기록
                createInventoryTransactions(transferCode, processDto.getFromWarehouseCode(), 
                        processDto.getToWarehouseCode(), item, processDto.getTransferDate());
            }
            
            log.info("창고이송 처리 완료 - 이송번호: {}", transferCode);
            return RespDto.success("창고이송이 완료되었습니다. 이송번호: " + transferCode, transferCode);
            
        } catch (Exception e) {
            log.error("창고이송 처리 중 오류 발생", e);
            return RespDto.fail("창고이송 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 이송번호 생성 (TR251114-0001 형식)
     */
    private String generateTransferCode(String transferDate) {
        // YYYYMMDD -> YYMMDD 변환
        String dateStr = transferDate.substring(2, 8);
        
        // 해당 날짜의 마지막 이송번호 조회
        String lastTransferCode = warehouseTransfersRepository.findLastTransferCodeByDate(dateStr);
        
        int sequence = 1;
        if (lastTransferCode != null) {
            // TR251114-0001에서 0001 부분 추출
            String lastSequenceStr = lastTransferCode.substring(lastTransferCode.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(lastSequenceStr) + 1;
        }
        
        return String.format("TR%s-%04d", dateStr, sequence);
    }
    
    /**
     * 출고창고 재고 차감
     */
    private void updateFromWarehouseQuantity(Integer warehouseCode, Integer itemCode, Integer quantity) {
        WarehouseItems warehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(warehouseCode, itemCode)
                .orElseThrow(() -> new RuntimeException("출고창고 품목을 찾을 수 없습니다."));
        
        warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() - quantity);
        warehouseItemsRepository.save(warehouseItem);
    }
    
    /**
     * 입고창고 재고 증가
     */
    private void updateToWarehouseQuantity(Integer warehouseCode, Integer itemCode, Integer quantity) {
        Optional<WarehouseItems> existing = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(warehouseCode, itemCode);
        
        if (existing.isPresent()) {
            // 기존 품목 수량 증가
            WarehouseItems warehouseItem = existing.get();
            warehouseItem.setCurrentQuantity(warehouseItem.getCurrentQuantity() + quantity);
            warehouseItemsRepository.save(warehouseItem);
        } else {
            // 새 품목 생성
            WarehouseItems newWarehouseItem = WarehouseItems.builder()
                    .warehouseCode(warehouseCode)
                    .itemCode(itemCode)
                    .currentQuantity(quantity)
                    .safeQuantity(0)
                    .build();
            warehouseItemsRepository.save(newWarehouseItem);
        }
    }
    
    /**
     * 월별재고마감 업데이트
     */
    private void updateMonthlyInventoryClosing(Integer fromWarehouseCode, Integer toWarehouseCode,
                                             Integer itemCode, Integer quantity, Integer amount, String yearMonth) {
        // 출고창고 월별재고마감 업데이트 (출고 증가)
        updateMonthlyClosingByWarehouse(fromWarehouseCode, itemCode, yearMonth, 0, 0, quantity, amount);
        
        // 입고창고 월별재고마감 업데이트 (입고 증가)  
        updateMonthlyClosingByWarehouse(toWarehouseCode, itemCode, yearMonth, quantity, amount, 0, 0);
    }
    
    /**
     * 창고별 월별재고마감 업데이트 (없으면 생성)
     */
    private void updateMonthlyClosingByWarehouse(Integer warehouseCode, Integer itemCode, String yearMonth,
                                               Integer inQuantity, Integer inAmount, Integer outQuantity, Integer outAmount) {
        // 먼저 해당 창고품목 조회
        Optional<WarehouseItems> warehouseItemOpt = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(warehouseCode, itemCode);
        
        if (warehouseItemOpt.isEmpty()) {
            log.warn("창고품목을 찾을 수 없습니다. 창고코드: {}, 품목코드: {}", warehouseCode, itemCode);
            return;
        }
        
        WarehouseItems warehouseItem = warehouseItemOpt.get();
        
        // 기존 월별재고마감 데이터 조회
        List<MonthlyInventoryClosing> closingList = monthlyInventoryClosingRepository
                .findByWarehouseCodeAndClosingYm(warehouseCode, yearMonth);
        
        // 해당 품목의 기존 데이터가 있는지 확인
        Optional<MonthlyInventoryClosing> existingClosing = closingList.stream()
                .filter(closing -> closing.getItemCode().equals(itemCode))
                .findFirst();
        
        if (existingClosing.isPresent()) {
            // 기존 데이터 업데이트
            MonthlyInventoryClosing closing = existingClosing.get();
            
            // 입출고 수량/금액 업데이트
            closing.setInQuantity(closing.getInQuantity() + inQuantity);
            closing.setInAmount(closing.getInAmount() + inAmount);
            closing.setOutQuantity(closing.getOutQuantity() + outQuantity);
            closing.setOutAmount(closing.getOutAmount() + outAmount);
            
            // 계산수량/금액 재계산
            int newCalQuantity = closing.getOpeningQuantity() + closing.getInQuantity() - closing.getOutQuantity();
            int newCalAmount = closing.getOpeningAmount() + closing.getInAmount() - closing.getOutAmount();
            
            closing.setCalQuantity(newCalQuantity);
            closing.setCalAmount(newCalAmount);
            
            monthlyInventoryClosingRepository.save(closing);
            
            log.info("월별재고마감 업데이트 완료 - 창고: {}, 품목: {}, 계산수량: {}", 
                    warehouseCode, itemCode, newCalQuantity);
            
        } else {
            // 새로운 월별재고마감 생성
            MonthlyInventoryClosing newClosing = MonthlyInventoryClosing.builder()
                    .warehouseItemCode(warehouseItem.getWarehouseItemCode())
                    .warehouseCode(warehouseCode)
                    .itemCode(itemCode)
                    .closingYm(yearMonth)
                    .openingQuantity(0)  // 이월수량 0
                    .openingAmount(0)    // 이월금액 0
                    .inQuantity(inQuantity)
                    .inAmount(inAmount)
                    .outQuantity(outQuantity)
                    .outAmount(outAmount)
                    .calQuantity(inQuantity - outQuantity)  // 계산수량 = 입고 - 출고
                    .calAmount(inAmount - outAmount)        // 계산금액 = 입고금액 - 출고금액
                    .actualQuantity(0)      // 실사수량 초기값
                    .actualUnitPrice(0)     // 실사단가 초기값
                    .actualAmount(0)        // 실사금액 초기값
                    .diffQuantity(0)        // 오차수량 초기값
                    .diffAmount(0)          // 오차금액 초기값
                    .isClosed(false)        // 마감여부 false
                    .build();
            
            monthlyInventoryClosingRepository.save(newClosing);
            
            log.info("월별재고마감 신규생성 완료 - 창고: {}, 품목: {}, 계산수량: {}", 
                    warehouseCode, itemCode, newClosing.getCalQuantity());
        }
    }
    
    /**
     * 재고수불부 기록 생성
     */
    private void createInventoryTransactions(String transferCode, Integer fromWarehouseCode, 
                                           Integer toWarehouseCode, TransferItemDto item, String transferDate) {
        // 출고창고의 warehouse_item_code 조회
        Optional<WarehouseItems> fromWarehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(fromWarehouseCode, item.getItemCode());
        
        // 입고창고의 warehouse_item_code 조회
        Optional<WarehouseItems> toWarehouseItem = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(toWarehouseCode, item.getItemCode());
        
        // 출고 기록
        if (fromWarehouseItem.isPresent()) {
            InventoryTransactions outTransaction = InventoryTransactions.builder()
                    .warehouseItemCode(fromWarehouseItem.get().getWarehouseItemCode())
                    .warehouseCode(fromWarehouseCode)
                    .itemCode(item.getItemCode())
                    .transactionDate(transferDate)
                    .transactionType("이송출고")
                    .quantity(-item.getQuantity()) // 음수로 기록
                    .unitPrice(item.getUnitPrice())
                    .amount(-item.getAmount()) // 음수로 기록
                    .note("창고이송 - " + transferCode)
                    .build();
            
            inventoryTransactionsRepository.save(outTransaction);
        }
        
        // 입고 기록
        if (toWarehouseItem.isPresent()) {
            InventoryTransactions inTransaction = InventoryTransactions.builder()
                    .warehouseItemCode(toWarehouseItem.get().getWarehouseItemCode())
                    .warehouseCode(toWarehouseCode)
                    .itemCode(item.getItemCode())
                    .transactionDate(transferDate)
                    .transactionType("이송입고")
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .amount(item.getAmount())
                    .note("창고이송 - " + transferCode)
                    .build();
            
            inventoryTransactionsRepository.save(inTransaction);
        }
    }
}