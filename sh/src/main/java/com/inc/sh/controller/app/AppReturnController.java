package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnOrderItemRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.AppReturnRequestReqDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnRequestRespDto;
import com.inc.sh.dto.returnRegistration.reqDto.AppReturnHistoryReqDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnHistoryListRespDto;
import com.inc.sh.dto.returnRegistration.respDto.AppReturnCancelRespDto;
import com.inc.sh.service.app.AppReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app/return")
@RequiredArgsConstructor
@Slf4j
public class AppReturnController {
    
    private final AppReturnService appReturnService;
    
    /**
     * [앱전용] 반품가능한 주문번호 조회
     * GET /api/v1/app/return/available-orders
     */
    @GetMapping("/available-orders")
    public ResponseEntity<RespDto<List<String>>> getAvailableOrders(
            @RequestParam("customerCode") Integer customerCode) {
        
        log.info("[앱] 반품가능 주문번호 조회 - customerCode: {}", customerCode);
        
        RespDto<List<String>> response = appReturnService.getAvailableOrders(customerCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 반품가능한 주문품목 조회
     * GET /api/v1/app/return/order-items
     */
    @GetMapping("/order-items")
    public ResponseEntity<RespDto<List<AppReturnOrderItemRespDto>>> getOrderItems(
            @RequestParam("orderNo") String orderNo,
            @RequestParam("customerCode") Integer customerCode) {
        
        log.info("[앱] 반품가능 주문품목 조회 - orderNo: {}, customerCode: {}", orderNo, customerCode);
        
        RespDto<List<AppReturnOrderItemRespDto>> response = appReturnService.getOrderItems(orderNo, customerCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 반품신청
     * POST /api/v1/app/return/request
     */
    @PostMapping("/request")
    public ResponseEntity<RespDto<AppReturnRequestRespDto>> requestReturn(@RequestBody AppReturnRequestReqDto request) {
        
        log.info("[앱] 반품신청 요청 - orderNo: {}, customerCode: {}, itemCode: {}", 
                request.getOrderNo(), request.getCustomerCode(), request.getItemCode());
        
        RespDto<AppReturnRequestRespDto> response = appReturnService.requestReturn(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 반품내역조회
     * GET /api/v1/app/return/history
     */
    @GetMapping("/history")
    public ResponseEntity<RespDto<AppReturnHistoryListRespDto>> getReturnHistory(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        log.info("[앱] 반품내역조회 - customerCode: {}, 기간: {} ~ {}, page: {}, size: {}", 
                customerCode, startDate, endDate, page, size);
        
        AppReturnHistoryReqDto request = AppReturnHistoryReqDto.builder()
                .customerCode(customerCode)
                .startDate(startDate)
                .endDate(endDate)
                .page(page)
                .size(size)
                .build();
        
        RespDto<AppReturnHistoryListRespDto> response = appReturnService.getReturnHistory(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 반품취소
     * DELETE /api/v1/app/return/cancel
     */
    @DeleteMapping("/cancel")
    public ResponseEntity<RespDto<AppReturnCancelRespDto>> cancelReturn(
            @RequestParam("returnNo") String returnNo,
            @RequestParam("customerCode") Integer customerCode) {
        
        log.info("[앱] 반품취소 요청 - returnNo: {}, customerCode: {}", returnNo, customerCode);
        
        RespDto<AppReturnCancelRespDto> response = appReturnService.cancelReturn(returnNo, customerCode);
        
        return ResponseEntity.ok(response);
    }
}