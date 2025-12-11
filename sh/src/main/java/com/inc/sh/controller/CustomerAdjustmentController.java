package com.inc.sh.controller;

import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSearchDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentDeleteReqDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSaveDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSaveReqDto;
import com.inc.sh.dto.customerAdjustment.respDto.CustomerAdjustmentBatchResult;
import com.inc.sh.dto.customerAdjustment.respDto.CustomerAdjustmentRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/erp/customer-adjustment")
@RequiredArgsConstructor
@Slf4j
public class CustomerAdjustmentController {

    private final CustomerAdjustmentService customerAdjustmentService;

    /**
     * 거래처조정처리 조회 (hqCode 필수, 날짜 범위 검색)
     * GET /api/v1/erp/customer-adjustment/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerAdjustmentRespDto>>> getCustomerAdjustmentList(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "adjustmentDateStart", required = false) String adjustmentDateStart,
            @RequestParam(value = "adjustmentDateEnd", required = false) String adjustmentDateEnd) {
        
        log.info("거래처조정처리 조회 요청 - hqCode: {}, 거래처: {}, 조정일자: {}~{}", 
                hqCode, customerCode, adjustmentDateStart, adjustmentDateEnd);
        
        CustomerAdjustmentSearchDto searchDto = CustomerAdjustmentSearchDto.builder()
                .hqCode(hqCode)
                .customerCode(customerCode)
                .adjustmentDateStart(adjustmentDateStart)
                .adjustmentDateEnd(adjustmentDateEnd)
                .build();
        
        RespDto<List<CustomerAdjustmentRespDto>> response = customerAdjustmentService.getCustomerAdjustmentList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 거래처조정처리 다중 저장 (신규/수정)
     * POST /api/v1/erp/customer-adjustment/save-batch
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerAdjustmentBatchResult>> saveCustomerAdjustments(
            @RequestBody CustomerAdjustmentSaveReqDto request) {
        
        log.info("거래처조정처리 다중 저장 요청 - 총 {}건", 
                request.getAdjustments() != null ? request.getAdjustments().size() : 0);
        
        // 요청 데이터 검증
        if (request.getAdjustments() == null || request.getAdjustments().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 조정처리 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (int i = 0; i < request.getAdjustments().size(); i++) {
            CustomerAdjustmentSaveReqDto.CustomerAdjustmentSaveItemDto item = request.getAdjustments().get(i);
            
            if (item.getCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 거래처코드는 필수입니다.", i + 1)));
            }
            if (item.getAdjustmentDate() == null || item.getAdjustmentDate().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 조정일자는 필수입니다.", i + 1)));
            }
            if (item.getAdjustmentAmount() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail(String.format("%d번째 항목: 조정금액은 필수입니다.", i + 1)));
            }
            
            // 기본값 설정
            if (item.getTransactionType() == null || item.getTransactionType().trim().isEmpty()) {
                item.setTransactionType("조정");
            }
        }
        
        RespDto<CustomerAdjustmentBatchResult> response = customerAdjustmentService.saveCustomerAdjustments(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 거래처조정처리 다중 삭제
     * DELETE /api/v1/erp/customer-adjustment/delete-batch
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<CustomerAdjustmentBatchResult>> deleteCustomerAdjustments(
            @RequestBody CustomerAdjustmentDeleteReqDto request) {
        
        log.info("거래처조정처리 다중 삭제 요청 - 총 {}건", 
                request.getTransactionCodes() != null ? request.getTransactionCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getTransactionCodes() == null || request.getTransactionCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 거래내역코드가 없습니다."));
        }
        
        // 중복 제거 및 null 제거
        List<Integer> validTransactionCodes = request.getTransactionCodes().stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        
        if (validTransactionCodes.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 거래내역코드가 없습니다."));
        }
        
        if (validTransactionCodes.size() != request.getTransactionCodes().size()) {
            log.info("중복/null 거래내역코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getTransactionCodes().size(), validTransactionCodes.size());
            request.setTransactionCodes(validTransactionCodes);
        }
        
        RespDto<CustomerAdjustmentBatchResult> response = customerAdjustmentService.deleteCustomerAdjustments(request);
        return ResponseEntity.ok(response);
    }
}