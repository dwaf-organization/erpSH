package com.inc.sh.controller;

import com.inc.sh.dto.monthlyInventoryStatus.reqDto.MonthlyInventoryStatusSearchDto;
import com.inc.sh.dto.monthlyInventoryStatus.respDto.MonthlyInventoryStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.MonthlyInventoryStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/monthly-inventory-status")
@RequiredArgsConstructor
@Slf4j
public class MonthlyInventoryStatusController {

    private final MonthlyInventoryStatusService monthlyInventoryStatusService;

    /**
     * 월재고현황 조회
     * GET /api/v1/erp/monthly-inventory-status/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<MonthlyInventoryStatusRespDto>>> getMonthlyInventoryStatusList(
            @RequestParam("closingYm") String closingYm,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "itemSearch", required = false) String itemSearch,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("월재고현황 조회 요청 - 마감년월: {}, 창고코드: {}, 분류코드: {}, 품목검색: {}, hqCode: {}", 
                closingYm, warehouseCode, categoryCode, itemSearch, hqCode);
        
        MonthlyInventoryStatusSearchDto searchDto = MonthlyInventoryStatusSearchDto.builder()
                .closingYm(closingYm)
                .warehouseCode(warehouseCode)
                .categoryCode(categoryCode)
                .itemSearch(itemSearch)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<MonthlyInventoryStatusRespDto>> response = monthlyInventoryStatusService.getMonthlyInventoryStatusList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}