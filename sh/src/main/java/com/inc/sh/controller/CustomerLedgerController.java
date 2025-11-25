package com.inc.sh.controller;

import com.inc.sh.dto.customerLedger.reqDto.CustomerLedgerSummarySearchDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerSummaryRespDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerDetailRespDto;
import com.inc.sh.dto.customerLedger.respDto.CustomerLedgerDailyRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerLedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-ledger")
@RequiredArgsConstructor
@Slf4j
public class CustomerLedgerController {

    private final CustomerLedgerService customerLedgerService;

    /**
     * 거래처별원장 집계 조회
     * GET /api/v1/erp/customer-ledger/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<RespDto<List<CustomerLedgerSummaryRespDto>>> getCustomerLedgerSummary(
            @RequestParam(value = "deliveryRequestDtStart", required = false) String deliveryRequestDtStart,
            @RequestParam(value = "deliveryRequestDtEnd", required = false) String deliveryRequestDtEnd,
            @RequestParam(value = "itemCode", required = false) Integer itemCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderStatus", required = false) String orderStatus) {
        
        log.info("거래처별원장 집계 조회 요청 - 납기일자: {}~{}, 품목코드: {}, 브랜드코드: {}, 거래처코드: {}, 주문상태: {}", 
                deliveryRequestDtStart, deliveryRequestDtEnd, itemCode, brandCode, customerCode, orderStatus);
        
        CustomerLedgerSummarySearchDto searchDto = CustomerLedgerSummarySearchDto.builder()
                .deliveryRequestDtStart(deliveryRequestDtStart)
                .deliveryRequestDtEnd(deliveryRequestDtEnd)
                .itemCode(itemCode)
                .brandCode(brandCode)
                .customerCode(customerCode)
                .orderStatus(orderStatus)
                .build();
        
        RespDto<List<CustomerLedgerSummaryRespDto>> response = customerLedgerService.getCustomerLedgerSummary(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처별원장 세부 조회
     * GET /api/v1/erp/customer-ledger/detail
     */
    @GetMapping("/detail")
    public ResponseEntity<RespDto<List<CustomerLedgerDetailRespDto>>> getCustomerLedgerDetail(
            @RequestParam(value = "deliveryRequestDtStart", required = false) String deliveryRequestDtStart,
            @RequestParam(value = "deliveryRequestDtEnd", required = false) String deliveryRequestDtEnd,
            @RequestParam(value = "itemCode", required = false) Integer itemCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderStatus", required = false) String orderStatus) {
        
        log.info("거래처별원장 세부 조회 요청 - 납기일자: {}~{}, 품목코드: {}, 브랜드코드: {}, 거래처코드: {}, 주문상태: {}", 
                deliveryRequestDtStart, deliveryRequestDtEnd, itemCode, brandCode, customerCode, orderStatus);
        
        CustomerLedgerSummarySearchDto searchDto = CustomerLedgerSummarySearchDto.builder()
                .deliveryRequestDtStart(deliveryRequestDtStart)
                .deliveryRequestDtEnd(deliveryRequestDtEnd)
                .itemCode(itemCode)
                .brandCode(brandCode)
                .customerCode(customerCode)
                .orderStatus(orderStatus)
                .build();
        
        RespDto<List<CustomerLedgerDetailRespDto>> response = customerLedgerService.getCustomerLedgerDetail(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처별원장 일자별 조회
     * GET /api/v1/erp/customer-ledger/daily
     */
    @GetMapping("/daily")
    public ResponseEntity<RespDto<List<CustomerLedgerDailyRespDto>>> getCustomerLedgerDaily(
            @RequestParam(value = "deliveryRequestDtStart", required = false) String deliveryRequestDtStart,
            @RequestParam(value = "deliveryRequestDtEnd", required = false) String deliveryRequestDtEnd,
            @RequestParam(value = "itemCode", required = false) Integer itemCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderStatus", required = false) String orderStatus) {
        
        log.info("거래처별원장 일자별 조회 요청 - 납기일자: {}~{}, 품목코드: {}, 브랜드코드: {}, 거래처코드: {}, 주문상태: {}", 
                deliveryRequestDtStart, deliveryRequestDtEnd, itemCode, brandCode, customerCode, orderStatus);
        
        CustomerLedgerSummarySearchDto searchDto = CustomerLedgerSummarySearchDto.builder()
                .deliveryRequestDtStart(deliveryRequestDtStart)
                .deliveryRequestDtEnd(deliveryRequestDtEnd)
                .itemCode(itemCode)
                .brandCode(brandCode)
                .customerCode(customerCode)
                .orderStatus(orderStatus)
                .build();
        
        RespDto<List<CustomerLedgerDailyRespDto>> response = customerLedgerService.getCustomerLedgerDaily(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}