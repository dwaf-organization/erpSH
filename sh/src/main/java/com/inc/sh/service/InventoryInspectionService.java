package com.inc.sh.service;

import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSearchDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionUpdateDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSaveReqDto;
import com.inc.sh.dto.inventoryInspection.respDto.InventoryInspectionRespDto;
import com.inc.sh.dto.inventoryInspection.respDto.InventoryInspectionBatchResult;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.MonthlyInventoryClosing;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import com.inc.sh.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryInspectionService {
    
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    private final ItemRepository itemRepository;
    private final MonthlyClosingService monthlyClosingService;
    
    /**
     * 재고실사 조회
     * 마감년월 + 창고코드 + 품목(코드 OR 품명) 조건으로 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<InventoryInspectionRespDto>> getInventoryInspectionList(InventoryInspectionSearchDto searchDto) {
        try {
            log.info("재고실사 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = monthlyInventoryClosingRepository.findInventoryInspectionByConditions(
                    searchDto.getClosingYm(),
                    searchDto.getWarehouseCode(),
                    searchDto.getItemSearch()
            );
            
            List<InventoryInspectionRespDto> responseList = results.stream()
                    .map(result -> InventoryInspectionRespDto.builder()
                            .closingCode((Integer) result[0])
                            .itemCode((Integer) result[1])
                            .itemName((String) result[2])
                            .categoryName((String) result[3])
                            .specification((String) result[4])
                            .unit((String) result[5])
                            .transactionType((String) result[6])
                            .openingQuantity(((Number) result[7]).intValue())
                            .openingAmount(((Number) result[8]).intValue())
                            .inQuantity(((Number) result[9]).intValue())
                            .inAmount(((Number) result[10]).intValue())
                            .outQuantity(((Number) result[11]).intValue())
                            .outAmount(((Number) result[12]).intValue())
                            .calQuantity(((Number) result[13]).intValue())
                            .calAmount(((Number) result[14]).intValue())
                            .actualQuantity(((Number) result[15]).intValue())
                            .actualUnitPrice(((Number) result[16]).intValue())
                            .actualAmount(((Number) result[17]).intValue())
                            .diffQuantity(((Number) result[18]).intValue())
                            .diffAmount(((Number) result[19]).intValue())
                            .build())
                    .collect(Collectors.toList());
            
            log.info("재고실사 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("재고실사 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("재고실사 조회 중 오류 발생", e);
            return RespDto.fail("재고실사 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 재고실사 다중 수정
     * 실사수량, 실사단가 수정 및 자동 계산 (개별 마감 상태 확인)
     */
    @Transactional
    public RespDto<InventoryInspectionBatchResult> updateInventoryInspections(InventoryInspectionSaveReqDto reqDto) {
        
        log.info("재고실사 다중 수정 시작 - 총 {}건", reqDto.getUpdates().size());
        
        List<InventoryInspectionBatchResult.InventoryInspectionSuccessDto> successData = new ArrayList<>();
        List<InventoryInspectionBatchResult.InventoryInspectionErrorDto> failData = new ArrayList<>();
        
        for (InventoryInspectionSaveReqDto.InventoryInspectionItemDto update : reqDto.getUpdates()) {
            try {
                // 개별 재고실사 수정 처리
                InventoryInspectionBatchResult.InventoryInspectionSuccessDto updatedInspection = updateSingleInventoryInspection(update);
                successData.add(updatedInspection);
                
                log.info("재고실사 수정 성공 - 마감코드: {}, 품목코드: {}", 
                        updatedInspection.getClosingCode(), updatedInspection.getItemCode());
                
            } catch (Exception e) {
                log.error("재고실사 수정 실패 - 마감코드: {}, 에러: {}", update.getClosingCode(), e.getMessage());
                
                // 에러 시 품목 정보 조회 시도
                InventoryInspectionBatchResult.InventoryInspectionErrorDto errorInfo = getInventoryInspectionErrorInfo(update.getClosingCode(), e.getMessage());
                failData.add(errorInfo);
            }
        }
        
        // 배치 결과 생성
        InventoryInspectionBatchResult result = InventoryInspectionBatchResult.builder()
                .totalCount(reqDto.getUpdates().size())
                .successCount(successData.size())
                .failCount(failData.size())
                .successData(successData)
                .failData(failData)
                .build();
        
        String message = String.format("재고실사 수정 완료 - 성공: %d건, 실패: %d건", 
                successData.size(), failData.size());
        
        log.info("재고실사 다중 수정 완료 - 총 {}건 중 성공 {}건, 실패 {}건", 
                reqDto.getUpdates().size(), successData.size(), failData.size());
        
        return RespDto.success(message, result);
    }
    
    /**
     * 개별 재고실사 수정 처리 (마감 상태 확인 및 자동 계산)
     */
    private InventoryInspectionBatchResult.InventoryInspectionSuccessDto updateSingleInventoryInspection(
            InventoryInspectionSaveReqDto.InventoryInspectionItemDto update) {
        
        // 월별재고마감 데이터 조회
        MonthlyInventoryClosing closing = monthlyInventoryClosingRepository
                .findById(update.getClosingCode())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 재고마감 데이터입니다."));
        
        // 마감 상태 체크 (개별 확인)
        String closingCheckMessage = monthlyClosingService.getClosingCheckMessage(
                closing.getClosingYm(), closing.getWarehouseCode());
        if (closingCheckMessage != null) {
            throw new RuntimeException(closingCheckMessage);
        }
        
        // 실사수량, 실사단가 수정
        if (update.getActualQuantity() != null) {
            closing.setActualQuantity(update.getActualQuantity());
        }
        if (update.getActualUnitPrice() != null) {
            closing.setActualUnitPrice(update.getActualUnitPrice());
        }
        
        // 실사금액 자동 계산
        if (closing.getActualQuantity() != null && closing.getActualUnitPrice() != null) {
            closing.setActualAmount(closing.getActualQuantity() * closing.getActualUnitPrice());
        }
        
        // 오차 자동 계산
        if (closing.getActualQuantity() != null && closing.getCalQuantity() != null) {
            closing.setDiffQuantity(closing.getActualQuantity() - closing.getCalQuantity());
        }
        if (closing.getActualAmount() != null && closing.getCalAmount() != null) {
            closing.setDiffAmount(closing.getActualAmount() - closing.getCalAmount());
        }
        
        // 저장
        monthlyInventoryClosingRepository.save(closing);
        
        // 품목명 조회 (응답용)
        String itemName = getItemNameSafely(closing.getItemCode());
        
        // 성공 응답 생성
        return InventoryInspectionBatchResult.InventoryInspectionSuccessDto.builder()
                .closingCode(closing.getClosingCode())
                .itemCode(closing.getItemCode())
                .itemName(itemName)
                .actualQuantity(closing.getActualQuantity())
                .actualUnitPrice(closing.getActualUnitPrice())
                .actualAmount(closing.getActualAmount())
                .diffQuantity(closing.getDiffQuantity())
                .diffAmount(closing.getDiffAmount())
                .build();
    }
    
    /**
     * 에러 정보 안전 조회 (수정 실패시 사용)
     */
    private InventoryInspectionBatchResult.InventoryInspectionErrorDto getInventoryInspectionErrorInfo(Integer closingCode, String errorMessage) {
        try {
            Optional<MonthlyInventoryClosing> closing = monthlyInventoryClosingRepository.findById(closingCode);
            if (closing.isPresent()) {
                String itemName = getItemNameSafely(closing.get().getItemCode());
                
                return InventoryInspectionBatchResult.InventoryInspectionErrorDto.builder()
                        .closingCode(closingCode)
                        .itemName(itemName)
                        .errorMessage(errorMessage)
                        .build();
            }
        } catch (Exception e) {
            log.error("에러 정보 조회 중 추가 오류 발생 - 마감코드: {}", closingCode, e);
        }
        
        // 조회 실패시 기본 정보
        return InventoryInspectionBatchResult.InventoryInspectionErrorDto.builder()
                .closingCode(closingCode)
                .itemName("조회 실패")
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * 품목명 안전 조회
     */
    private String getItemNameSafely(Integer itemCode) {
        try {
            return itemRepository.findByItemCode(itemCode)
                    .getItemName();
        } catch (Exception e) {
            log.error("품목명 조회 중 오류 발생 - itemCode: {}", itemCode, e);
            return "품목코드-" + itemCode;
        }
    }
}