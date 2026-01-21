package com.inc.sh.controller;

import com.inc.sh.dto.transactionStatement.reqDto.TransactionStatementSearchDto;
import com.inc.sh.dto.transactionStatement.respDto.TransactionStatementRespDto;
import com.inc.sh.service.TransactionStatementService;
import com.inc.sh.common.dto.RespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/transaction-statement")
@RequiredArgsConstructor
@Slf4j
public class TransactionStatementController {

    private final TransactionStatementService transactionStatementService;

    /**
     * 거래명세서 조회
     * GET /api/v1/erp/transaction-statement/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<TransactionStatementRespDto>>> getTransactionStatement(
            @RequestParam("deliveryRequestDt") String deliveryRequestDt,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("거래명세서 조회 요청 - 납기일자: {}, 거래처: {}, 브랜드: {}, 본사: {}", 
                deliveryRequestDt, customerCode, brandCode, hqCode);
        
        // 요청 데이터 검증
        if (deliveryRequestDt == null || deliveryRequestDt.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("납기일자는 필수입니다."));
        }
        
        if (deliveryRequestDt.length() != 8) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("납기일자는 YYYYMMDD 형식이어야 합니다."));
        }
        
        if (hqCode == null) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("본사코드는 필수입니다."));
        }
        
        TransactionStatementSearchDto searchDto = TransactionStatementSearchDto.builder()
                .deliveryRequestDt(deliveryRequestDt)
                .customerCode(customerCode)
                .brandCode(brandCode)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<TransactionStatementRespDto>> response = 
                transactionStatementService.getTransactionStatement(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래명세서 요약 조회
     * GET /api/v1/erp/transaction-statement/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<RespDto<TransactionStatementService.TransactionStatementSummaryDto>> getTransactionStatementSummary(
            @RequestParam("deliveryRequestDt") String deliveryRequestDt,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("거래명세서 요약 조회 요청 - 납기일자: {}, 거래처: {}, 브랜드: {}, 본사: {}", 
                deliveryRequestDt, customerCode, brandCode, hqCode);
        
        // 요청 데이터 검증
        if (deliveryRequestDt == null || deliveryRequestDt.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("납기일자는 필수입니다."));
        }
        
        TransactionStatementSearchDto searchDto = TransactionStatementSearchDto.builder()
                .deliveryRequestDt(deliveryRequestDt)
                .customerCode(customerCode)
                .brandCode(brandCode)
                .hqCode(hqCode)
                .build();
        
        RespDto<TransactionStatementService.TransactionStatementSummaryDto> response = 
                transactionStatementService.getTransactionStatementSummary(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}