package com.inc.sh.controller;

import com.inc.sh.dto.returnManagement.reqDto.ReturnSearchDto;
import com.inc.sh.dto.returnManagement.reqDto.ReturnUpdateDto;
import com.inc.sh.dto.returnManagement.respDto.ReturnRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.ReturnOrderSearchDto;
import com.inc.sh.dto.returnRegistration.reqDto.ReturnRegistrationSaveDto;
import com.inc.sh.dto.returnRegistration.respDto.ReturnOrderItemRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.ReturnManagementService;
import com.inc.sh.service.ReturnRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/return")
@RequiredArgsConstructor
@Slf4j
public class ReturnController {

    private final ReturnManagementService returnManagementService;
    private final ReturnRegistrationService returnRegistrationService;

    // ==================== 반품관리 (조회/수정/삭제) ====================

    /**
     * 반품 조회
     * GET /api/v1/erp/return/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<ReturnRespDto>>> getReturnList(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "status", required = false) String status) {
        
        log.info("반품 조회 요청 - 기간: {} ~ {}, 거래처코드: {}, 진행상태: {}", 
                startDate, endDate, customerCode, status);
        
        ReturnSearchDto searchDto = ReturnSearchDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .customerCode(customerCode)
                .status(status)
                .build();
        
        RespDto<List<ReturnRespDto>> response = returnManagementService.getReturnList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 반품 수정
     * PUT /api/v1/erp/return/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<String>> updateReturn(@RequestBody ReturnUpdateDto updateDto) {
        
        log.info("반품 수정 요청 - 반품코드: {}", updateDto.getReturnCode());
        
        RespDto<String> response = returnManagementService.updateReturn(updateDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 반품 삭제 (미승인 상태만 삭제 가능)
     * DELETE /api/v1/erp/return/{returnNo}
     */
    @DeleteMapping("/{returnNo}")
    public ResponseEntity<RespDto<String>> deleteReturn(@PathVariable String returnNo) {
        
        log.info("반품 삭제 요청 - 반품번호: {}", returnNo);
        
        RespDto<String> response = returnManagementService.deleteReturn(returnNo);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 반품등록 ====================

    /**
     * 반품등록용 주문품목 조회
     * GET /api/v1/erp/return/order-items
     */
    @GetMapping("/order-items")
    public ResponseEntity<RespDto<List<ReturnOrderItemRespDto>>> getOrderItemsForReturn(
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderNo", required = false) String orderNo) {
        
        log.info("반품등록용 주문품목 조회 요청 - 거래처코드: {}, 주문번호: {}", customerCode, orderNo);
        
        ReturnOrderSearchDto searchDto = ReturnOrderSearchDto.builder()
                .customerCode(customerCode)
                .orderNo(orderNo)
                .build();
        
        RespDto<List<ReturnOrderItemRespDto>> response = returnRegistrationService.getOrderItemsForReturn(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 반품 등록 (신규)
     * POST /api/v1/erp/return/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveReturn(@RequestBody ReturnRegistrationSaveDto saveDto) {
        
        log.info("반품 등록 요청 - 거래처코드: {}, 품목코드: {}", saveDto.getCustomerCode(), saveDto.getItemCode());
        
        RespDto<String> response = returnRegistrationService.saveReturn(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}