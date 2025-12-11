package com.inc.sh.controller;

import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSearchDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositDeleteReqDto;
import com.inc.sh.dto.customerDeposit.reqDto.CustomerDepositSaveDto;
import com.inc.sh.dto.customerDeposit.respDto.CustomerDepositBatchResult;
import com.inc.sh.dto.customerDeposit.respDto.CustomerDepositRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.CustomerDepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(value = "depositTypeCode", required = false) Integer depositTypeCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("거래처수금처리 조회 요청 - 거래처: {}, 기간: {}~{}, 입금유형: {}, hqCode: {}", 
                customerCode, startDate, endDate, depositTypeCode, hqCode);
        
        CustomerDepositSearchDto searchDto = CustomerDepositSearchDto.builder()
                .customerCode(customerCode)
                .startDate(startDate)
                .endDate(endDate)
                .depositTypeCode(depositTypeCode)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<CustomerDepositRespDto>> response = customerDepositService.getCustomerDepositList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 거래처수금처리 다중 저장 (신규/수정)
     * POST /api/v1/erp/customer-deposit/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerDepositBatchResult>> saveCustomerDeposits(@RequestBody CustomerDepositSaveDto request) {
        
        log.info("거래처수금처리 다중 저장 요청 - 총 {}건", 
                request.getDeposits() != null ? request.getDeposits().size() : 0);
        
        // 요청 데이터 검증
        if (request.getDeposits() == null || request.getDeposits().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 입금 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (CustomerDepositSaveDto.CustomerDepositItemDto deposit : request.getDeposits()) {
            if (deposit.getCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("거래처코드는 필수입니다."));
            }
            if (deposit.getDepositMethod() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("입금방법은 필수입니다."));
            }
            if (deposit.getDepositDate() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("입금일자는 필수입니다."));
            }
            if (deposit.getDepositAmount() == null || deposit.getDepositAmount() <= 0) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("입금금액은 0원보다 커야 합니다."));
            }
        }
        
        RespDto<CustomerDepositBatchResult> response = customerDepositService.saveCustomerDeposits(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 거래처수금처리 다중 삭제
     * DELETE /api/v1/erp/customer-deposit/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<CustomerDepositBatchResult>> deleteCustomerDeposits(@RequestBody CustomerDepositDeleteReqDto request) {
        
        log.info("거래처수금처리 다중 삭제 요청 - 총 {}건", 
                request.getDepositIds() != null ? request.getDepositIds().size() : 0);
        
        // 요청 데이터 검증
        if (request.getDepositIds() == null || request.getDepositIds().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 입금코드가 없습니다."));
        }
        
        // 중복 제거
        List<Integer> uniqueDepositIds = request.getDepositIds().stream()
                .distinct()
                .collect(Collectors.toList());
        
        if (uniqueDepositIds.size() != request.getDepositIds().size()) {
            log.info("중복된 입금코드 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getDepositIds().size(), uniqueDepositIds.size());
            request.setDepositIds(uniqueDepositIds);
        }
        
        RespDto<CustomerDepositBatchResult> response = customerDepositService.deleteCustomerDeposits(request);
        return ResponseEntity.ok(response);
    }
}