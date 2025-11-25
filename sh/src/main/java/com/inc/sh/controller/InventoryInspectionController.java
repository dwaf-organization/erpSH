package com.inc.sh.controller;

import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionSearchDto;
import com.inc.sh.dto.inventoryInspection.reqDto.InventoryInspectionUpdateDto;
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
     * 재고실사 수정
     * PUT /api/v1/erp/inventory-inspection/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<String>> updateInventoryInspection(@RequestBody InventoryInspectionUpdateDto updateDto) {
        
        log.info("재고실사 수정 요청 - 마감코드: {}, 실사수량: {}, 실사단가: {}", 
                updateDto.getClosingCode(), updateDto.getActualQuantity(), updateDto.getActualUnitPrice());
        
        RespDto<String> response = inventoryInspectionService.updateInventoryInspection(updateDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}