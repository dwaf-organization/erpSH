package com.inc.sh.service;

import com.inc.sh.dto.monthlyClosing.reqDto.MonthlyClosingSearchDto;
import com.inc.sh.dto.monthlyClosing.reqDto.MonthlyClosingProcessDto;
import com.inc.sh.dto.monthlyClosing.respDto.MonthlyClosingRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.repository.MonthlyInventoryClosingRepository;
import com.inc.sh.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlyClosingService {
    
    private final MonthlyInventoryClosingRepository monthlyInventoryClosingRepository;
    private final WarehouseRepository warehouseRepository;
    
    /**
     * 월재고마감 현황 조회
     * 창고별 그룹핑하여 마감 상태 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<MonthlyClosingRespDto>> getMonthlyClosingList(MonthlyClosingSearchDto searchDto) {
        try {
            log.info("월재고마감 현황 조회 시작 - 조건: {}", searchDto);
            
            List<Object[]> results = monthlyInventoryClosingRepository.findMonthlyClosingByConditions(
                    searchDto.getClosingYm(),
                    searchDto.getWarehouseCode()
            );
            
            List<MonthlyClosingRespDto> responseList = results.stream()
                    .map(result -> {
                        String closedAt = null;
                        if (result[4] != null) {
                            if (result[4] instanceof java.sql.Date) {
                                closedAt = ((java.sql.Date) result[4]).toString(); // 2025-11-14 형태
                            } else {
                                closedAt = result[4].toString();
                            }
                        }
                        
                        return MonthlyClosingRespDto.builder()
                                .warehouseCode((Integer) result[0])
                                .warehouseName((String) result[1])
                                .closingYm((String) result[2])
                                .isClosed(((Number) result[3]).intValue())
                                .closedAt(closedAt)
                                .closedUser((String) result[5])
                                .build();
                    })
                    .collect(Collectors.toList());
            
            log.info("월재고마감 현황 조회 완료 - 조회 건수: {}", responseList.size());
            return RespDto.success("월재고마감 현황 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("월재고마감 현황 조회 중 오류 발생", e);
            return RespDto.fail("월재고마감 현황 조회 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 월재고마감 처리
     * 현재 상태에 따라 마감/마감취소 토글
     */
    @Transactional
    public RespDto<String> processMonthlyClosing(MonthlyClosingProcessDto processDto) {
        try {
            log.info("월재고마감 처리 시작 - 년월: {}, 창고코드: {}", 
                    processDto.getClosingYm(), processDto.getWarehouseCode());
            
            // 창고명 조회
            String warehouseName = warehouseRepository.findById(processDto.getWarehouseCode())
                    .map(warehouse -> warehouse.getWarehouseName())
                    .orElse("알 수 없는 창고");
            
            // 현재 마감 상태 조회
            Integer currentStatus = monthlyInventoryClosingRepository.findClosingStatusByWarehouseAndYm(
                    processDto.getClosingYm(), processDto.getWarehouseCode());
            
            if (currentStatus == null) {
                return RespDto.fail("해당 년월의 재고 데이터가 존재하지 않습니다.");
            }
            
            // 상태 토글 결정
            Integer newStatus = (currentStatus == 0) ? 1 : 0;
            LocalDateTime closedAt = (newStatus == 1) ? LocalDateTime.now() : null;
            String closedUser = (newStatus == 1) ? "system" : null; // TODO: 실제 사용자 정보로 변경
            
            // 해당 창고의 해당 년월 모든 데이터 업데이트
            int updatedCount = monthlyInventoryClosingRepository.updateClosingStatusByWarehouseAndYm(
                    processDto.getClosingYm(),
                    processDto.getWarehouseCode(),
                    newStatus,
                    closedAt,
                    closedUser
            );
            
            String action = (newStatus == 1) ? "마감 확정" : "마감 취소";
            String message = String.format("%s %s가 %s되었습니다. (처리 건수: %d개)", 
                    processDto.getClosingYm(), warehouseName, action, updatedCount);
            
            log.info("월재고마감 처리 완료 - {}", message);
            return RespDto.success(message, action + " 완료");
            
        } catch (Exception e) {
            log.error("월재고마감 처리 중 오류 발생 - 년월: {}, 창고코드: {}", 
                    processDto.getClosingYm(), processDto.getWarehouseCode(), e);
            return RespDto.fail("월재고마감 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 마감 상태 확인 (다른 서비스에서 호출용)
     */
    @Transactional(readOnly = true)
    public boolean isClosedWarehouse(String closingYm, Integer warehouseCode) {
        try {
            Integer status = monthlyInventoryClosingRepository.findClosingStatusByWarehouseAndYm(
                    closingYm, warehouseCode);
            return status != null && status == 1;
        } catch (Exception e) {
            log.error("마감 상태 확인 중 오류 발생 - 년월: {}, 창고코드: {}", closingYm, warehouseCode, e);
            return false;
        }
    }
    
    /**
     * 마감 상태 확인 후 에러 메시지 반환
     */
    @Transactional(readOnly = true)
    public String getClosingCheckMessage(String closingYm, Integer warehouseCode) {
        try {
            if (isClosedWarehouse(closingYm, warehouseCode)) {
                String warehouseName = warehouseRepository.findById(warehouseCode)
                        .map(warehouse -> warehouse.getWarehouseName())
                        .orElse("알 수 없는 창고");
                return String.format("%s %s이 마감되었습니다.", closingYm, warehouseName);
            }
            return null;
        } catch (Exception e) {
            log.error("마감 체크 메시지 생성 중 오류 발생", e);
            return "마감 상태 확인 중 오류가 발생했습니다.";
        }
    }
}