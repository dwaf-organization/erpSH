package com.inc.sh.controller;

import com.inc.sh.dto.transactionStatement.reqDto.TransactionStatementExcelReqDto;
import com.inc.sh.service.TransactionStatementExcelService;
import com.inc.sh.common.dto.RespDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/v1/erp/transaction-statement")
@RequiredArgsConstructor
@Slf4j
public class TransactionStatementExcelController {

    private final TransactionStatementExcelService transactionStatementExcelService;

    /**
     * 거래명세표 엑셀 출력 (주문별 분리)
     * POST /api/v1/erp/transaction-statement/excel
     */
    @PostMapping("/excel")
    public ResponseEntity<?> generateTransactionStatementExcel(@RequestBody TransactionStatementExcelReqDto request) {
        
        log.info("거래명세표 엑셀 생성 요청 - 주문개수: {}, 본사: {}", 
                request.getOrderNumbers() != null ? request.getOrderNumbers().size() : 0, 
                request.getHqCode());
        
        // 요청 데이터 검증
        if (request.getOrderNumbers() == null || request.getOrderNumbers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("주문번호 리스트는 필수입니다."));
        }
        
        if (request.getHqCode() == null) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("본사코드는 필수입니다."));
        }
        
        try {
            // 엑셀 파일 생성
            RespDto<Map<String, byte[]>> response = transactionStatementExcelService.generateTransactionStatementExcel(request);
            
            if (response.getCode() != 1 || response.getData() == null || response.getData().isEmpty()) {
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, byte[]> excelFiles = response.getData();
            
            // 단일 파일인 경우 바로 다운로드
            if (excelFiles.size() == 1) {
                Map.Entry<String, byte[]> entry = excelFiles.entrySet().iterator().next();
                String orderNo = entry.getKey();
                byte[] excelData = entry.getValue();
                
                String fileName = "거래명세표_" + orderNo + "_" + 
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8))
                        .body(new ByteArrayResource(excelData));
            }
            
            // 여러 파일인 경우 ZIP으로 압축
            else {
                ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
                
                try (ZipOutputStream zip = new ZipOutputStream(zipOutputStream)) {
                    for (Map.Entry<String, byte[]> entry : excelFiles.entrySet()) {
                        String orderNo = entry.getKey();
                        byte[] excelData = entry.getValue();
                        
                        String entryFileName = "거래명세표_" + orderNo + ".xlsx";
                        ZipEntry zipEntry = new ZipEntry(entryFileName);
                        zip.putNextEntry(zipEntry);
                        zip.write(excelData);
                        zip.closeEntry();
                    }
                }
                
                String zipFileName = "거래명세표_" + excelFiles.size() + "건_" +
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".zip";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename*=UTF-8''" + URLEncoder.encode(zipFileName, StandardCharsets.UTF_8))
                        .body(new ByteArrayResource(zipOutputStream.toByteArray()));
            }
            
        } catch (Exception e) {
            log.error("거래명세표 엑셀 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body(RespDto.fail("엑셀 파일 생성 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}