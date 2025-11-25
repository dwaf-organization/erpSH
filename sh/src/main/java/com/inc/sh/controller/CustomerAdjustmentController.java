package com.inc.sh.controller;

import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSearchDto;
import com.inc.sh.dto.customerAdjustment.reqDto.CustomerAdjustmentSaveDto;
import com.inc.sh.dto.customerAdjustment.respDto.CustomerAdjustmentRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerAdjustmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-adjustment")
@RequiredArgsConstructor
@Slf4j
public class CustomerAdjustmentController {

    private final CustomerAdjustmentService customerAdjustmentService;

    /**
     * 거래처조정처리 조회
     * GET /api/v1/erp/customer-adjustment/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerAdjustmentRespDto>>> getCustomerAdjustmentList(
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "adjustmentDate", required = false) String adjustmentDate) {
        
        log.info("거래처조정처리 조회 요청 - 거래처: {}, 조정일자: {}", customerCode, adjustmentDate);
        
        CustomerAdjustmentSearchDto searchDto = CustomerAdjustmentSearchDto.builder()
                .customerCode(customerCode)
                .adjustmentDate(adjustmentDate)
                .build();
        
        RespDto<List<CustomerAdjustmentRespDto>> response = customerAdjustmentService.getCustomerAdjustmentList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처조정처리 저장 (신규/수정)
     * POST /api/v1/erp/customer-adjustment/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveCustomerAdjustment(@RequestBody CustomerAdjustmentSaveDto saveDto) {
        
        log.info("거래처조정처리 저장 요청 - 거래내역코드: {}, 거래처: {}, 조정금액: {}", 
                saveDto.getTransactionCode(), saveDto.getCustomerCode(), saveDto.getAdjustmentAmount());
        
        // 거래유형이 없으면 기본값 설정
        if (saveDto.getTransactionType() == null || saveDto.getTransactionType().isEmpty()) {
            saveDto.setTransactionType("조정");
        }
        
        RespDto<String> response = customerAdjustmentService.saveCustomerAdjustment(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처조정처리 삭제
     * DELETE /api/v1/erp/customer-adjustment/{transactionCode}
     */
    @DeleteMapping("/{transactionCode}")
    public ResponseEntity<RespDto<String>> deleteCustomerAdjustment(@PathVariable("transactionCode") Integer transactionCode) {
        
        log.info("거래처조정처리 삭제 요청 - 거래내역코드: {}", transactionCode);
        
        RespDto<String> response = customerAdjustmentService.deleteCustomerAdjustment(transactionCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}