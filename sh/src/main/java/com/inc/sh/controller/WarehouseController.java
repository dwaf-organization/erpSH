package com.inc.sh.controller;

import com.inc.sh.dto.warehouse.reqDto.WarehouseSearchDto;
import com.inc.sh.dto.warehouse.reqDto.WarehouseReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseListRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseSaveRespDto;
import com.inc.sh.dto.warehouse.respDto.WarehouseDeleteRespDto;
import com.inc.sh.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/warehouse")
@RequiredArgsConstructor
@Slf4j
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 창고 목록 조회
     * GET /api/v1/erp/warehouse/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<WarehouseListRespDto>>> getWarehouseList(
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "distCenterCode", required = false) Integer distCenterCode) {
        
        log.info("창고 목록 조회 요청 - warehouseCode: {}, distCenterCode: {}", warehouseCode, distCenterCode);
        
        WarehouseSearchDto searchDto = WarehouseSearchDto.builder()
                .warehouseCode(warehouseCode)
                .distCenterCode(distCenterCode)
                .build();
        
        RespDto<List<WarehouseListRespDto>> response = warehouseService.getWarehouseList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고 저장 (신규/수정)
     * POST /api/v1/erp/warehouse/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<WarehouseSaveRespDto>> saveWarehouse(
            @Valid @RequestBody WarehouseReqDto request) {
        
        if (request.getWarehouseCode() == null) {
            log.info("창고 신규 등록 요청 - warehouseName: {}, distCenterCode: {}", 
                    request.getWarehouseName(), request.getDistCenterCode());
        } else {
            log.info("창고 수정 요청 - warehouseCode: {}, warehouseName: {}", 
                    request.getWarehouseCode(), request.getWarehouseName());
        }
        
        RespDto<WarehouseSaveRespDto> response = warehouseService.saveWarehouse(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고 삭제 (하드 삭제)
     * DELETE /api/v1/erp/warehouse/{warehouseCode}
     */
    @DeleteMapping("/{warehouseCode}")
    public ResponseEntity<RespDto<WarehouseDeleteRespDto>> deleteWarehouse(
            @PathVariable("warehouseCode") Integer warehouseCode) {
        
        log.info("창고 삭제 요청 - warehouseCode: {}", warehouseCode);
        
        RespDto<WarehouseDeleteRespDto> response = warehouseService.deleteWarehouse(warehouseCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}