package com.inc.sh.controller;

import com.inc.sh.dto.inventory.reqDto.InventorySearchDto;
import com.inc.sh.dto.inventory.reqDto.InventorySaveDto;
import com.inc.sh.dto.inventory.respDto.InventoryRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 재고등록 조회
     * GET /api/v1/erp/inventory/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<InventoryRespDto>>> getInventoryList(
            @RequestParam("closingYm") String closingYm,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "itemSearch", required = false) String itemSearch) {
        
        log.info("재고등록 조회 요청 - 마감년월: {}, 창고코드: {}, 품목검색: {}", 
                closingYm, warehouseCode, itemSearch);
        
        InventorySearchDto searchDto = InventorySearchDto.builder()
                .closingYm(closingYm)
                .warehouseCode(warehouseCode)
                .itemSearch(itemSearch)
                .build();
        
        RespDto<List<InventoryRespDto>> response = inventoryService.getInventoryList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 재고등록 저장
     * POST /api/v1/erp/inventory/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveInventory(@RequestBody InventorySaveDto saveDto) {
        
        log.info("재고등록 저장 요청 - 창고코드: {}, 마감년월: {}, 품목수: {}", 
                saveDto.getWarehouseCode(), saveDto.getClosingYm(), 
                saveDto.getItems() != null ? saveDto.getItems().size() : 0);
        
        RespDto<String> response = inventoryService.saveInventory(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 재고 삭제
     * DELETE /api/v1/erp/inventory/delete/{warehouseItemCode}
     */
    @DeleteMapping("/delete/{warehouseItemCode}")
    public ResponseEntity<RespDto<String>> deleteInventory(@PathVariable("warehouseItemCode") Integer warehouseItemCode) {
        
        log.info("재고 삭제 요청 - 창고품목코드: {}", warehouseItemCode);
        
        RespDto<String> response = inventoryService.deleteInventory(warehouseItemCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}