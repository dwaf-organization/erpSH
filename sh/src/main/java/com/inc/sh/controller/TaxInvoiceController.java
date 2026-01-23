package com.inc.sh.controller;

import com.inc.sh.dto.taxInvoice.reqDto.TaxInvoiceReqDto;
import com.inc.sh.dto.taxInvoice.reqDto.TaxInvoiceExcelReqDto;
import com.inc.sh.dto.taxInvoice.respDto.TaxInvoiceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.TaxInvoiceService;
import com.inc.sh.service.TaxInvoiceExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/erp/tax-invoice")
@RequiredArgsConstructor
@Slf4j
public class TaxInvoiceController {
    
    private final TaxInvoiceService taxInvoiceService;
    private final TaxInvoiceExcelService taxInvoiceExcelService;
    
    /**
     * 전자세금계산서 발행 데이터 조회 (프로그램용)
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
     * 전자세금계산서 발행 데이터 조회 (POST 방식, 프로그램용)
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
    
    /**
     * 전자세금계산서 엑셀 다운로드
     * GET /api/v1/erp/tax-invoice/excel
     */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadTaxInvoiceExcel(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("taxType") String taxType,
            @RequestParam("issueDate") String issueDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "itemCodes", required = false) List<Integer> itemCodes) {
        
        log.info("전자세금계산서 엑셀 다운로드 요청 - startDate: {}, endDate: {}, hqCode: {}, taxType: {}, issueDate: {}", 
                startDate, endDate, hqCode, taxType, issueDate);
        
        TaxInvoiceExcelReqDto reqDto = TaxInvoiceExcelReqDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .hqCode(hqCode)
                .customerCode(customerCode)
                .itemCodes(itemCodes)
                .taxType(taxType)
                .issueDate(issueDate)
                .build();
        
        RespDto<byte[]> response = taxInvoiceExcelService.generateTaxInvoiceExcel(reqDto);
        
        if (response.getCode() != 1) {
            log.error("엑셀 생성 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 파일명 생성 및 UTF-8 인코딩
            String filename = String.format("전자세금계산서_%s_%s.xlsx", taxType, issueDate);
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            
            // Content-Disposition 헤더 생성 (RFC 6266 표준)
            String contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(response.getData());
                    
        } catch (Exception e) {
            log.error("파일명 인코딩 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 전자세금계산서 엑셀 다운로드 (POST 방식)
     * POST /api/v1/erp/tax-invoice/excel
     */
    @PostMapping("/excel")
    public ResponseEntity<byte[]> downloadTaxInvoiceExcelByPost(
            @Valid @RequestBody TaxInvoiceExcelReqDto reqDto) {
        
        log.info("전자세금계산서 엑셀 다운로드 요청 (POST) - startDate: {}, endDate: {}, hqCode: {}, taxType: {}, issueDate: {}", 
                reqDto.getStartDate(), reqDto.getEndDate(), reqDto.getHqCode(), 
                reqDto.getTaxType(), reqDto.getIssueDate());
        
        RespDto<byte[]> response = taxInvoiceExcelService.generateTaxInvoiceExcel(reqDto);
        
        if (response.getCode() != 1) {
            log.error("엑셀 생성 실패: {}", response.getMessage());
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 파일명 생성 및 UTF-8 인코딩
            String filename = String.format("전자세금계산서_%s_%s.xlsx", 
                    reqDto.getTaxType(), reqDto.getIssueDate());
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            
            // Content-Disposition 헤더 생성 (RFC 6266 표준)
            String contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(response.getData());
                    
        } catch (Exception e) {
            log.error("파일명 인코딩 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}