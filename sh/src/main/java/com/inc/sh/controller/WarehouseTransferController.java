package com.inc.sh.controller;

import com.inc.sh.dto.warehouseTransfer.reqDto.WarehouseTransferListSearchDto;
import com.inc.sh.dto.warehouseTransfer.reqDto.WarehouseTransferProcessDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseItemRespDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseTransferListRespDto;
import com.inc.sh.dto.warehouseTransfer.respDto.WarehouseTransferItemRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.WarehouseTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/warehouse-transfer")
@RequiredArgsConstructor
@Slf4j
public class WarehouseTransferController {

    private final WarehouseTransferService warehouseTransferService;

    /**
     * 창고이송현황 목록 조회
     * GET /api/v1/erp/warehouse-transfer/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<WarehouseTransferListRespDto>>> getWarehouseTransferList(
            @RequestParam("startYm") String startYm,
            @RequestParam("endYm") String endYm,
            @RequestParam(value = "fromWarehouseCode", required = false) Integer fromWarehouseCode,
            @RequestParam(value = "toWarehouseCode", required = false) Integer toWarehouseCode) {
        
        log.info("창고이송현황 목록 조회 요청 - 기간: {}~{}, 출고창고: {}, 입고창고: {}", 
                startYm, endYm, fromWarehouseCode, toWarehouseCode);
        
        WarehouseTransferListSearchDto searchDto = WarehouseTransferListSearchDto.builder()
                .startYm(startYm)
                .endYm(endYm)
                .fromWarehouseCode(fromWarehouseCode)
                .toWarehouseCode(toWarehouseCode)
                .build();
        
        RespDto<List<WarehouseTransferListRespDto>> response = warehouseTransferService.getWarehouseTransferList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고이송품목 상세 조회
     * GET /api/v1/erp/warehouse-transfer/item-list
     */
    @GetMapping("/item-list")
    public ResponseEntity<RespDto<List<WarehouseTransferItemRespDto>>> getWarehouseTransferItems(
            @RequestParam("transferCode") String transferCode) {
        
        log.info("창고이송품목 상세 조회 요청 - 이송번호: {}", transferCode);
        
        RespDto<List<WarehouseTransferItemRespDto>> response = warehouseTransferService.getWarehouseTransferItems(transferCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 출고창고 품목 조회
     * GET /api/v1/erp/warehouse-transfer/warehouse-items
     */
    @GetMapping("/warehouse-items")
    public ResponseEntity<RespDto<List<WarehouseItemRespDto>>> getWarehouseItems(
            @RequestParam("warehouseCode") Integer warehouseCode) {
        
        log.info("출고창고 품목 조회 요청 - 창고코드: {}", warehouseCode);
        
        RespDto<List<WarehouseItemRespDto>> response = warehouseTransferService.getWarehouseItems(warehouseCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 창고이송 처리
     * POST /api/v1/erp/warehouse-transfer/process
     */
    @PostMapping("/process")
    public ResponseEntity<RespDto<String>> processWarehouseTransfer(@RequestBody WarehouseTransferProcessDto processDto) {
        
        log.info("창고이송 처리 요청 - 출고창고: {}, 입고창고: {}, 품목수: {}", 
                processDto.getFromWarehouseCode(), processDto.getToWarehouseCode(),
                processDto.getItems() != null ? processDto.getItems().size() : 0);
        
        RespDto<String> response = warehouseTransferService.processWarehouseTransfer(processDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}