package com.inc.sh.controller;

import com.inc.sh.dto.customerCollectionStatus.reqDto.CustomerCollectionStatusSearchDto;
import com.inc.sh.dto.customerCollectionStatus.respDto.CustomerCollectionStatusRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerCollectionStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer-collection-status")
@RequiredArgsConstructor
@Slf4j
public class CustomerCollectionStatusController {

    private final CustomerCollectionStatusService customerCollectionStatusService;

    /**
     * 거래처별잔액현황 조회
     * GET /api/v1/erp/customer-collection-status/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerCollectionStatusRespDto>>> getCustomerCollectionStatusList(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode) {
        
        log.info("거래처별잔액현황 조회 요청 - 기간: {}~{}, 거래처: {}", startDate, endDate, customerCode);
        
        CustomerCollectionStatusSearchDto searchDto = CustomerCollectionStatusSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerCode(customerCode)
                .build();
        
        RespDto<List<CustomerCollectionStatusRespDto>> response = customerCollectionStatusService.getCustomerCollectionStatusList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}