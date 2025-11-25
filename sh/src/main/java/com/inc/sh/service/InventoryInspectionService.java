package com.inc.sh.service;

import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSearchDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionUpdateDto;
import com.inc.sh.dto.inventoryInspection.respDto.InventoryInspectionRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.entity.MonthlyInventoryClosing;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryInspectionService {
    
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
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
     * 재고실사 수정
     * 실사수량, 실사단가 수정 및 자동 계산
     */
    @Transactional
    public RespDto<String> updateInventoryInspection(InventoryInspectionUpdateDto updateDto) {
        try {
            log.info("재고실사 수정 시작 - 마감코드: {}", updateDto.getClosingCode());
            
            // 월별재고마감 데이터 조회
            Optional<MonthlyInventoryClosing> optionalClosing = monthlyInventoryClosingRepository
                    .findById(updateDto.getClosingCode());
            
            if (optionalClosing.isEmpty()) {
                return RespDto.fail("존재하지 않는 재고마감 데이터입니다.");
            }
            
            MonthlyInventoryClosing closing = optionalClosing.get();
            
            // 마감 상태 체크
            String closingCheckMessage = monthlyClosingService.getClosingCheckMessage(
                    closing.getClosingYm(), closing.getWarehouseCode());
            if (closingCheckMessage != null) {
                return RespDto.fail(closingCheckMessage);
            }
            
            // 실사수량, 실사단가 수정
            if (updateDto.getActualQuantity() != null) {
                closing.setActualQuantity(updateDto.getActualQuantity());
            }
            if (updateDto.getActualUnitPrice() != null) {
                closing.setActualUnitPrice(updateDto.getActualUnitPrice());
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
            
            monthlyInventoryClosingRepository.save(closing);
            
            log.info("재고실사 수정 완료 - 마감코드: {}, 실사수량: {}, 실사단가: {}", 
                    updateDto.getClosingCode(), updateDto.getActualQuantity(), updateDto.getActualUnitPrice());
            
            return RespDto.success("재고실사가 수정되었습니다.", "수정 완료");
            
        } catch (Exception e) {
            log.error("재고실사 수정 중 오류 발생 - 마감코드: {}", updateDto.getClosingCode(), e);
            return RespDto.fail("재고실사 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}