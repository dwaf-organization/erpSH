package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.customer.reqDto.CustomerDeleteReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSaveReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSearchDto;
import com.inc.sh.dto.customer.respDto.CustomerBatchResult;
import com.inc.sh.dto.customer.respDto.CustomerDeleteRespDto;
import com.inc.sh.dto.customer.respDto.CustomerRespDto;
import com.inc.sh.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/customer")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 거래처 목록 조회
     * GET /api/v1/erp/customer/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<CustomerRespDto>>> getCustomerList(
            @RequestParam(value = "brandCode", required = false) Integer brandCode,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "closeDtYn", required = false) Integer closeDtYn,
            @RequestParam(value = "orderBlockYn", required = false) Integer orderBlockYn) {
        
        log.info("거래처 목록 조회 요청 - brandCode: {}, customerName: {}, closeDtYn: {}, orderBlockYn: {}", 
                brandCode, customerName, closeDtYn, orderBlockYn);
        
        CustomerSearchDto searchDto = CustomerSearchDto.builder()
                .brandCode(brandCode)
                .customerName(customerName)
                .closeDtYn(closeDtYn)
                .orderBlockYn(orderBlockYn)
                .build();
        
        RespDto<List<CustomerRespDto>> response = customerService.getCustomerList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처 상세 조회
     * GET /api/v1/erp/customer/detail/{customerCode}
     */
    @GetMapping("/detail/{customerCode}")
    public ResponseEntity<RespDto<CustomerRespDto>> getCustomer(
            @PathVariable("customerCode") Integer customerCode) {
        
        log.info("거래처 상세 조회 요청 - customerCode: {}", customerCode);
        
        RespDto<CustomerRespDto> response = customerService.getCustomer(customerCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    /**
     * 거래처 다중 저장 (신규/수정)
     * POST /api/v1/erp/customer/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerBatchResult>> saveCustomers(@RequestBody CustomerSaveReqDto request) {
        
        log.info("거래처 다중 저장 요청 - 총 {}건", 
                request.getCustomers() != null ? request.getCustomers().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCustomers() == null || request.getCustomers().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 거래처 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (CustomerSaveReqDto.CustomerItemDto customer : request.getCustomers()) {
            if (customer.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("본사코드는 필수입니다."));
            }
            if (customer.getBrandCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("브랜드코드는 필수입니다."));
            }
            if (customer.getCustomerName() == null || customer.getCustomerName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("거래처명은 필수입니다."));
            }
            if (customer.getOwnerName() == null || customer.getOwnerName().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("대표자는 필수입니다."));
            }
            if (customer.getBizNum() == null || customer.getBizNum().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("사업자번호는 필수입니다."));
            }
            if (customer.getDistCenterCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("물류센터코드는 필수입니다."));
            }
        }
        
        RespDto<CustomerBatchResult> response = customerService.saveCustomers(request);
        return ResponseEntity.ok(response);
    }
    /**
     * 거래처 다중 삭제
     * DELETE /api/v1/erp/customer/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<CustomerBatchResult>> deleteCustomers(@RequestBody CustomerDeleteReqDto request) {
        
        log.info("거래처 다중 삭제 요청 - 총 {}건", 
                request.getCustomerCodes() != null ? request.getCustomerCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCustomerCodes() == null || request.getCustomerCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 거래처 코드가 없습니다."));
        }
        
        RespDto<CustomerBatchResult> response = customerService.deleteCustomers(request);
        return ResponseEntity.ok(response);
    }
}