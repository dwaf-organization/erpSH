package com.inc.sh.service;

import com.inc.sh.dto.inventory.reqDto.InventorySearchDto;
import com.inc.sh.dto.inventory.reqDto.InventorySaveDto;
import com.inc.sh.dto.inventory.reqDto.InventoryItemDto;
import com.inc.sh.dto.inventory.respDto.InventoryRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.WarehouseItems;
import com.inc.sh.entity.MonthlyInventoryClosing;
import com.inc.sh.entity.InventoryTransactions;
import com.inc.sh.repository.WarehouseItemsRepository;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import com.inc.sh.repository.InventoryTransactionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final WarehouseItemsRepository warehouseItemsRepository;
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    private final InventoryTransactionsRepository inventoryTransactionsRepository;
    private final MonthlyClosingService monthlyClosingService;
    
    /**
     * 재고등록 조회
     * 마감년월 + 창고코드 + 품목(코드 OR 품명) 조건으로 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<InventoryRespDto>> getInventoryList(InventorySearchDto searchDto) {
        try {
            log.info("재고등록 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = monthlyInventoryClosingRepository.findInventoryByConditions(
                    searchDto.getClosingYm(),
                    searchDto.getWarehouseCode(),
                    searchDto.getItemSearch()
            );
            
            List<InventoryRespDto> responseList = results.stream()
                    .map(result -> InventoryRespDto.builder()
                            .warehouseItemCode((Integer) result[0])
                            .itemCode((Integer) result[1])
                            .itemName((String) result[2])
                            .specification((String) result[3])
                            .unit((String) result[4])  // purchase_unit 값
                            .actualUnitPrice(((Number) result[5]).intValue())
                            .actualQuantity(((Number) result[6]).intValue())
                            .actualAmount(((Number) result[7]).intValue())
                            .currentQuantity(((Number) result[8]).intValue())
                            .safeQuantity(((Number) result[9]).intValue())
                            .warehouseName((String) result[10])
                            .closingYm((String) result[11])
                            .build())
                    .collect(Collectors.toList());
            
            log.info("재고등록 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("재고등록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("재고등록 조회 중 오류 발생", e);
            return RespDto.fail("재고등록 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 재고등록 저장
     * 창고품목 기반으로 월별재고마감 테이블 생성/수정
     */
    @Transactional
    public RespDto<String> saveInventory(InventorySaveDto saveDto) {
        try {
            log.info("재고등록 저장 시작 - 창고코드: {}, 년월: {}, 품목수: {}", 
                    saveDto.getWarehouseCode(), saveDto.getClosingYm(), 
                    saveDto.getItems() != null ? saveDto.getItems().size() : 0);
            
            // 마감 상태 체크
            String closingCheckMessage = monthlyClosingService.getClosingCheckMessage(
                    saveDto.getClosingYm(), saveDto.getWarehouseCode());
            if (closingCheckMessage != null) {
                return RespDto.fail(closingCheckMessage);
            }
            
            List<String> processedItems = new ArrayList<>();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            for (InventoryItemDto itemDto : saveDto.getItems()) {
                try {
                    // 창고품목 확인/생성/수정
                    WarehouseItems warehouseItem = getOrCreateWarehouseItem(
                            saveDto.getWarehouseCode(), itemDto.getItemCode(), itemDto);
                    
                    // 월별재고마감 데이터 확인/생성/수정
                    MonthlyInventoryClosing closing = getOrCreateMonthlyClosing(
                            warehouseItem, saveDto.getClosingYm(), itemDto);
                    
                    // 재고수불부 기록 추가
                    createInventoryTransaction(warehouseItem, itemDto, currentDate);
                    
                    processedItems.add(itemDto.getItemCode().toString());
                    
                    log.info("재고 처리 완료 - 품목코드: {}", itemDto.getItemCode());
                    
                } catch (Exception e) {
                    log.error("품목 처리 중 오류 발생 - 품목코드: {}", itemDto.getItemCode(), e);
                    throw e;
                }
            }
            
            log.info("재고등록 저장 완료 - 처리된 품목: {}", processedItems);
            return RespDto.success("재고등록 저장이 완료되었습니다. 처리된 품목: " + processedItems.size() + "개", 
                    "저장 완료");
            
        } catch (Exception e) {
            log.error("재고등록 저장 중 오류 발생", e);
            return RespDto.fail("재고등록 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 재고 삭제
     * 창고품목과 관련된 모든 데이터 삭제
     */
    @Transactional
    public RespDto<String> deleteInventory(Integer warehouseItemCode) {
        try {
            log.info("재고 삭제 시작 - 창고품목코드: {}", warehouseItemCode);
            
            // 창고품목 존재 확인
            Optional<WarehouseItems> warehouseItem = warehouseItemsRepository.findById(warehouseItemCode);
            if (warehouseItem.isEmpty()) {
                return RespDto.fail("존재하지 않는 창고품목입니다.");
            }
            
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            // 재고수불부에 삭제 기록 추가
            InventoryTransactions deleteTransaction = InventoryTransactions.builder()
                    .warehouseItemCode(warehouseItemCode)
                    .warehouseCode(warehouseItem.get().getWarehouseCode())
                    .itemCode(warehouseItem.get().getItemCode())
                    .transactionDate(currentDate)
                    .transactionType("재고삭제")
                    .quantity(0)
                    .unitPrice(0)
                    .amount(0)
                    .note("재고등록에서 품목 삭제")
                    .build();
            
            inventoryTransactionsRepository.save(deleteTransaction);
            
            // 월별재고마감 데이터 삭제
            monthlyInventoryClosingRepository.deleteByWarehouseItemCode(warehouseItemCode);
            
            // 기타 재고수불부 데이터 삭제 (삭제 기록 제외)
            inventoryTransactionsRepository.deleteByWarehouseItemCode(warehouseItemCode);
            
            // 창고품목 삭제
            warehouseItemsRepository.deleteById(warehouseItemCode);
            
            log.info("재고 삭제 완료 - 창고품목코드: {}", warehouseItemCode);
            return RespDto.success("재고가 삭제되었습니다.", "삭제 완료");
            
        } catch (Exception e) {
            log.error("재고 삭제 중 오류 발생 - 창고품목코드: {}", warehouseItemCode, e);
            return RespDto.fail("재고 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 창고품목 확인/생성/수정
     */
    private WarehouseItems getOrCreateWarehouseItem(Integer warehouseCode, Integer itemCode, InventoryItemDto itemDto) {
        Optional<WarehouseItems> existing = warehouseItemsRepository
                .findByWarehouseCodeAndItemCode(warehouseCode, itemCode);
        
        if (existing.isPresent()) {
            // 기존 창고품목 수정
            WarehouseItems warehouseItem = existing.get();
            warehouseItem.setCurrentQuantity(itemDto.getActualQuantity()); // 현재고량을 실사수량으로 업데이트
            if (itemDto.getSafeQuantity() != null) {
                warehouseItem.setSafeQuantity(itemDto.getSafeQuantity());   // 안전재고량 업데이트
            }
            return warehouseItemsRepository.save(warehouseItem);
        }
        
        // 새로운 창고품목 생성
        WarehouseItems newWarehouseItem = WarehouseItems.builder()
                .warehouseCode(warehouseCode)
                .itemCode(itemCode)
                .currentQuantity(itemDto.getActualQuantity())  // 현재고량을 실사수량으로 설정
                .safeQuantity(itemDto.getSafeQuantity() != null ? itemDto.getSafeQuantity() : 0)  // 안전재고량 설정
                .build();
        
        return warehouseItemsRepository.save(newWarehouseItem);
    }
    
    /**
     * 월별재고마감 확인/생성/수정
     */
    private MonthlyInventoryClosing getOrCreateMonthlyClosing(
            WarehouseItems warehouseItem, String closingYm, InventoryItemDto itemDto) {
        
        Optional<MonthlyInventoryClosing> existing = monthlyInventoryClosingRepository
                .findByWarehouseItemCodeAndClosingYm(warehouseItem.getWarehouseItemCode(), closingYm);
        
        if (existing.isPresent()) {
            // 기존 데이터 수정
            MonthlyInventoryClosing closing = existing.get();
            closing.setActualQuantity(itemDto.getActualQuantity());
            closing.setActualUnitPrice(itemDto.getActualUnitPrice());
            closing.setActualAmount(itemDto.getActualAmount());
            
            return monthlyInventoryClosingRepository.save(closing);
        }
        
        // 새로운 월별재고마감 생성
        MonthlyInventoryClosing newClosing = MonthlyInventoryClosing.builder()
                .warehouseItemCode(warehouseItem.getWarehouseItemCode())
                .warehouseCode(warehouseItem.getWarehouseCode())
                .itemCode(warehouseItem.getItemCode())
                .closingYm(closingYm)
                .openingQuantity(0)  // 최초등록시 이월은 0
                .openingAmount(0)
                .inQuantity(0)
                .inAmount(0)
                .outQuantity(0)
                .outAmount(0)
                .calQuantity(itemDto.getActualQuantity())
                .calAmount(itemDto.getActualAmount())
                .actualQuantity(itemDto.getActualQuantity())
                .actualUnitPrice(itemDto.getActualUnitPrice())
                .actualAmount(itemDto.getActualAmount())
                .diffQuantity(0)
                .diffAmount(0)
                .isClosed(false)
                .build();
        
        return monthlyInventoryClosingRepository.save(newClosing);
    }
    
    /**
     * 재고수불부 기록 생성
     */
    private void createInventoryTransaction(WarehouseItems warehouseItem, 
                                          InventoryItemDto itemDto, String currentDate) {
        InventoryTransactions transaction = InventoryTransactions.builder()
                .warehouseItemCode(warehouseItem.getWarehouseItemCode())
                .warehouseCode(warehouseItem.getWarehouseCode())
                .itemCode(warehouseItem.getItemCode())
                .transactionDate(currentDate)
                .transactionType("재고등록")
                .quantity(itemDto.getActualQuantity())
                .unitPrice(itemDto.getActualUnitPrice())
                .amount(itemDto.getActualAmount())
                .note("재고등록 페이지에서 입력")
                .build();
        
        inventoryTransactionsRepository.save(transaction);
    }
}