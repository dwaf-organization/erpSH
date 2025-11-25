package com.inc.sh.controller;

import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSearchDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSaveDto;
import com.inc.sh.dto.customerDeposit.respDto.CustomerDepositRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerDepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-deposit")
@RequiredArgsConstructor
@Slf4j
public class CustomerDepositController {

    private final CustomerDepositService customerDepositService;

    /**
     * 거래처수금처리 조회
     * GET /api/v1/erp/customer-deposit/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerDepositRespDto>>> getCustomerDepositList(
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "depositTypeCode", required = false) Integer depositTypeCode) {
        
        log.info("거래처수금처리 조회 요청 - 거래처: {}, 기간: {}~{}, 입금유형: {}", 
                customerCode, startDate, endDate, depositTypeCode);
        
        CustomerDepositSearchDto searchDto = CustomerDepositSearchDto.builder()
                .customerCode(customerCode)
                .startDate(startDate)
                .endDate(endDate)
                .depositTypeCode(depositTypeCode)
                .build();
        
        RespDto<List<CustomerDepositRespDto>> response = customerDepositService.getCustomerDepositList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처수금처리 저장 (신규/수정)
     * POST /api/v1/erp/customer-deposit/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveCustomerDeposit(@RequestBody CustomerDepositSaveDto saveDto) {
        
        log.info("거래처수금처리 저장 요청 - 입금코드: {}, 거래처: {}, 금액: {}", 
                saveDto.getDepositId(), saveDto.getCustomerCode(), saveDto.getDepositAmount());
        
        RespDto<String> response = customerDepositService.saveCustomerDeposit(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처수금처리 삭제
     * DELETE /api/v1/erp/customer-deposit/{depositId}
     */
    @DeleteMapping("/{depositId}")
    public ResponseEntity<RespDto<String>> deleteCustomerDeposit(@PathVariable("depositId") Integer depositId) {
        
        log.info("거래처수금처리 삭제 요청 - 입금코드: {}", depositId);
        
        RespDto<String> response = customerDepositService.deleteCustomerDeposit(depositId);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}