package com.inc.sh.controller;

import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSaveReqDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSearchDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionUpdateDto;
import com.inc.sh.dto.inventoryInspection.respDto.InventoryInspectionBatchResult;
import com.inc.sh.dto.inventoryInspection.respDto.InventoryInspectionRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.InventoryInspectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/inventory-inspection")
@RequiredArgsConstructor
@Slf4j
public class InventoryInspectionController {

    private final InventoryInspectionService inventoryInspectionService;

    /**
     * 재고실사 조회
     * GET /api/v1/erp/inventory-inspection/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<InventoryInspectionRespDto>>> getInventoryInspectionList(
            @RequestParam("closingYm") String closingYm,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "itemSearch", required = false) String itemSearch) {
        
        log.info("재고실사 조회 요청 - 마감년월: {}, 창고코드: {}, 품목검색: {}", 
                closingYm, warehouseCode, itemSearch);
        
        InventoryInspectionSearchDto searchDto = InventoryInspectionSearchDto.builder()
                .closingYm(closingYm)
                .warehouseCode(warehouseCode)
                .itemSearch(itemSearch)
                .build();
        
        RespDto<List<InventoryInspectionRespDto>> response = inventoryInspectionService.getInventoryInspectionList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 재고실사 다중 수정 (마감 상태 확인 포함)
     * PUT /api/v1/erp/inventory-inspection/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<InventoryInspectionBatchResult>> updateInventoryInspections(@RequestBody InventoryInspectionSaveReqDto request) {
        
        log.info("재고실사 다중 수정 요청 - 총 {}건", 
                request.getUpdates() != null ? request.getUpdates().size() : 0);
        
        // 요청 데이터 검증
        if (request.getUpdates() == null || request.getUpdates().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("수정할 재고실사 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (InventoryInspectionSaveReqDto.InventoryInspectionItemDto update : request.getUpdates()) {
            if (update.getClosingCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("마감코드는 필수입니다."));
            }
            if (update.getActualQuantity() == null && update.getActualUnitPrice() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("실사수량 또는 실사단가 중 하나는 필수입니다."));
            }
        }
        
        RespDto<InventoryInspectionBatchResult> response = inventoryInspectionService.updateInventoryInspections(request);
        return ResponseEntity.ok(response);
    }
}