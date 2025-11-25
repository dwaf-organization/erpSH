package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.customer.reqDto.CustomerReqDto;
import com.inc.sh.dto.customer.reqDto.CustomerSearchDto;
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
     * 거래처 저장 (신규/수정)
     * POST /api/v1/erp/customer/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<CustomerRespDto>> saveCustomer(
            @Valid @RequestBody CustomerReqDto request) {
        
        if (request.getCustomerCode() == null) {
            log.info("거래처 신규 등록 요청 - customerName: {}, brandCode: {}", 
                    request.getCustomerName(), request.getBrandCode());
        } else {
            log.info("거래처 수정 요청 - customerCode: {}, customerName: {}", 
                    request.getCustomerCode(), request.getCustomerName());
        }
        
        RespDto<CustomerRespDto> response = customerService.saveCustomer(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처 삭제 (폐기일자 입력)
     * DELETE /api/v1/erp/customer/{customerCode}
     */
    @DeleteMapping("/{customerCode}")
    public ResponseEntity<RespDto<CustomerDeleteRespDto>> deleteCustomer(
            @PathVariable("customerCode") Integer customerCode) {
        
        log.info("거래처 삭제 요청 - customerCode: {}", customerCode);
        
        RespDto<CustomerDeleteRespDto> response = customerService.deleteCustomer(customerCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}