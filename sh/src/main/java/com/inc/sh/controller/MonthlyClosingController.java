package com.inc.sh.controller;

import com.inc.sh.dto.monthlyClosing.reqDto.MonthlyClosingSearchDto;
import com.inc.sh.dto.monthlyClosing.reqDto.MonthlyClosingProcessDto;
import com.inc.sh.dto.monthlyClosing.respDto.MonthlyClosingRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.MonthlyClosingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/monthly-closing")
@RequiredArgsConstructor
@Slf4j
public class MonthlyClosingController {

    private final MonthlyClosingService monthlyClosingService;

    /**
     * 월재고마감 현황 조회
     * GET /api/v1/erp/monthly-closing/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<MonthlyClosingRespDto>>> getMonthlyClosingList(
            @RequestParam("closingYm") String closingYm,
            @RequestParam(value = "warehouseCode", required = false) Integer warehouseCode) {
        
        log.info("월재고마감 현황 조회 요청 - 마감년월: {}, 창고코드: {}", closingYm, warehouseCode);
        
        MonthlyClosingSearchDto searchDto = MonthlyClosingSearchDto.builder()
                .closingYm(closingYm)
                .warehouseCode(warehouseCode)
                .build();
        
        RespDto<List<MonthlyClosingRespDto>> response = monthlyClosingService.getMonthlyClosingList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 월재고마감 처리
     * POST /api/v1/erp/monthly-closing/process
     */
    @PostMapping("/process")
    public ResponseEntity<RespDto<String>> processMonthlyClosing(@RequestBody MonthlyClosingProcessDto processDto) {
        
        log.info("월재고마감 처리 요청 - 마감년월: {}, 창고코드: {}", 
                processDto.getClosingYm(), processDto.getWarehouseCode());
        
        RespDto<String> response = monthlyClosingService.processMonthlyClosing(processDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}