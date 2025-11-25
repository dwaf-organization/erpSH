package com.inc.sh.controller;

import com.inc.sh.dto.customerBalance.reqDto.CustomerBalanceUpdateDto;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-balance")
@RequiredArgsConstructor
@Slf4j
public class CustomerBalanceController {

    private final CustomerBalanceService customerBalanceService;

    /**
     * 브랜드별 후입금 거래처 미수잔액 조회
     * GET /api/v1/erp/customer-balance/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerBalanceRespDto>>> getCustomerBalanceList(
            @RequestParam("brandCode") Integer brandCode) {
        
        log.info("거래처미수잔액 조회 요청 - 브랜드코드: {}", brandCode);
        
        RespDto<List<CustomerBalanceRespDto>> response = customerBalanceService.getCustomerBalanceList(brandCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처 미수잔액 수정
     * POST /api/v1/erp/customer-balance/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> updateCustomerBalance(@RequestBody CustomerBalanceUpdateDto updateDto) {
        
        log.info("거래처미수잔액 수정 요청 - 거래처코드: {}, 기초미수금: {}", 
                updateDto.getCustomerCode(), updateDto.getBalanceAmt());
        
        RespDto<String> response = customerBalanceService.updateCustomerBalance(updateDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}