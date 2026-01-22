package com.inc.sh.controller;

import com.inc.sh.dto.taxInvoice.reqDto.TaxInvoiceReqDto;
import com.inc.sh.dto.taxInvoice.respDto.TaxInvoiceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.TaxInvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/tax-invoice")
@RequiredArgsConstructor
@Slf4j
public class TaxInvoiceController {
    
    private final TaxInvoiceService taxInvoiceService;
    
    /**
     * 전자세금계산서 발행 데이터 조회
     * GET /api/v1/erp/tax-invoice/data
     */
    @GetMapping("/data")
    public ResponseEntity<RespDto<List<TaxInvoiceRespDto>>> getTaxInvoiceData(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "itemCodes", required = false) List<Integer> itemCodes) {
        
        log.info("전자세금계산서 발행 데이터 조회 요청 - startDate: {}, endDate: {}, hqCode: {}, customerCode: {}, itemCodes: {}", 
                startDate, endDate, hqCode, customerCode, itemCodes);
        
        TaxInvoiceReqDto reqDto = TaxInvoiceReqDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .hqCode(hqCode)
                .customerCode(customerCode)
                .itemCodes(itemCodes)
                .build();
        
        RespDto<List<TaxInvoiceRespDto>> response = taxInvoiceService.getTaxInvoiceData(reqDto);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 전자세금계산서 발행 데이터 조회 (POST 방식)
     * POST /api/v1/erp/tax-invoice/data
     */
    @PostMapping("/data")
    public ResponseEntity<RespDto<List<TaxInvoiceRespDto>>> getTaxInvoiceDataByPost(
            @Valid @RequestBody TaxInvoiceReqDto reqDto) {
        
        log.info("전자세금계산서 발행 데이터 조회 요청 (POST) - startDate: {}, endDate: {}, hqCode: {}, customerCode: {}, itemCodes: {}", 
                reqDto.getStartDate(), reqDto.getEndDate(), reqDto.getHqCode(), 
                reqDto.getCustomerCode(), reqDto.getItemCodes());
        
        RespDto<List<TaxInvoiceRespDto>> response = taxInvoiceService.getTaxInvoiceData(reqDto);
        
        return ResponseEntity.ok(response);
    }
}