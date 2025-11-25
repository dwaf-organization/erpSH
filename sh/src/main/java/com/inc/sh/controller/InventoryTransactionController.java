package com.inc.sh.controller;

import com.inc.sh.dto.inventoryTransaction.reqDto.InventoryTransactionSearchDto;
import com.inc.sh.dto.inventoryTransaction.respDto.InventoryTransactionRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/inventory-transaction")
@RequiredArgsConstructor
@Slf4j
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;

    /**
     * 재고수불부 조회
     * GET /api/v1/erp/inventory-transaction/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<RespDto<List<InventoryTransactionRespDto>>> getInventoryTransactionSummary(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "itemCodeSearch", required = false) String itemCodeSearch) {
        
        log.info("재고수불부 조회 요청 - 조회기간: {}~{}, 창고코드: {}, 분류코드: {}, 품목코드검색: {}", 
                startDate, endDate, warehouseCode, categoryCode, itemCodeSearch);
        
        InventoryTransactionSearchDto searchDto = InventoryTransactionSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .warehouseCode(warehouseCode)
                .categoryCode(categoryCode)
                .itemCodeSearch(itemCodeSearch)
                .build();
        
        RespDto<List<InventoryTransactionRespDto>> response = inventoryTransactionService.getInventoryTransactionSummary(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}