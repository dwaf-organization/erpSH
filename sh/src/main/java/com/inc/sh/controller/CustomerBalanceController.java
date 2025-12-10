package com.inc.sh.controller;

import com.inc.sh.dto.customerBalance.reqDto.CustomerBalanceUpdateDto;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceBatchResult;
import com.inc.sh.dto.customerBalance.respDto.CustomerBalanceRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
     * 거래처잔액 다중 수정 (덮어쓰기)
     * POST /api/v1/erp/customer-balance/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerBalanceBatchResult>> updateCustomerBalances(@RequestBody CustomerBalanceUpdateDto request) {
        
        log.info("거래처잔액 다중 수정 요청 - 총 {}건", 
                request.getCustomers() != null ? request.getCustomers().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCustomers() == null || request.getCustomers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("수정할 거래처 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (CustomerBalanceUpdateDto.CustomerBalanceItemDto customer : request.getCustomers()) {
            if (customer.getCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("거래처코드는 필수입니다."));
            }
            if (customer.getBalanceAmt() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("기초미수금은 필수입니다."));
            }
        }
        
        // 중복 제거
        List<CustomerBalanceUpdateDto.CustomerBalanceItemDto> uniqueCustomers = request.getCustomers().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueCustomers.size() != request.getCustomers().size()) {
            log.info("중복된 거래처코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getCustomers().size(), uniqueCustomers.size());
            request.setCustomers(uniqueCustomers);
        }
        
        RespDto<CustomerBalanceBatchResult> response = customerBalanceService.updateCustomerBalances(request);
        return ResponseEntity.ok(response);
    }
}