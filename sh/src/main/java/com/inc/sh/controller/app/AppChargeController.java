package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.charge.reqDto.AppTransactionHistoryReqDto;
import com.inc.sh.dto.charge.respDto.AppTransactionHistoryRespDto;
import com.inc.sh.service.app.AppChargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/app/charge")
@RequiredArgsConstructor
@Slf4j
public class AppChargeController {
    
    private final AppChargeService appChargeService;
    
    /**
     * [앱전용] 거래내역조회
     * GET /api/v1/app/charge/transaction-history
     */
    @GetMapping("/transaction-history")
    public ResponseEntity<RespDto<AppTransactionHistoryRespDto>> getTransactionHistory(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("startTransactionDate") String startTransactionDate,
            @RequestParam("endTransactionDate") String endTransactionDate,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("[앱] 거래내역조회 - customerCode: {}, 기간: {} ~ {}, page: {}, size: {}", 
                customerCode, startTransactionDate, endTransactionDate, page, size);
        
        AppTransactionHistoryReqDto request = AppTransactionHistoryReqDto.builder()
                .customerCode(customerCode)
                .startTransactionDate(startTransactionDate)
                .endTransactionDate(endTransactionDate)
                .page(page)
                .size(size)
                .build();
        
        RespDto<AppTransactionHistoryRespDto> response = appChargeService.getTransactionHistory(request);
        
        return ResponseEntity.ok(response);
    }
}