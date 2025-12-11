package com.inc.sh.controller;

import com.inc.sh.dto.customerDepositStatus.reqDto.CustomerDepositStatusSearchDto;
import com.inc.sh.dto.customerDepositStatus.respDto.CustomerDepositStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerDepositStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-deposit-status")
@RequiredArgsConstructor
@Slf4j
public class CustomerDepositStatusController {

    private final CustomerDepositStatusService customerDepositStatusService;

    /**
     * 거래처별수금현황 조회
     * GET /api/v1/erp/customer-deposit-status/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerDepositStatusRespDto>>> getCustomerDepositStatusList(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam(value = "depositMethod", required = false) Integer depositMethod,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("거래처별수금현황 조회 요청 - 기간: {}~{}, 거래처: {}, 브랜드: {}, 입금유형: {}, hqCode: {}", 
                startDate, endDate, customerCode, brandCode, depositMethod, hqCode);
        
        CustomerDepositStatusSearchDto searchDto = CustomerDepositStatusSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerCode(customerCode)
                .brandCode(brandCode)
                .depositMethod(depositMethod)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<CustomerDepositStatusRespDto>> response = customerDepositStatusService.getCustomerDepositStatusList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}