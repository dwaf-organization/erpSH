package com.inc.sh.controller;

import com.inc.sh.dto.logisticsPayment.reqDto.LogisticsPaymentSearchDto;
import com.inc.sh.dto.logisticsPayment.respDto.LogisticsPaymentRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.LogisticsPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/logistics-payment")
@RequiredArgsConstructor
@Slf4j
public class LogisticsPaymentController {

    private final LogisticsPaymentService logisticsPaymentService;

    /**
     * 물류대금마감현황 조회
     * GET /api/v1/erp/logistics-payment/status
     */
    @GetMapping("/status")
    public ResponseEntity<RespDto<List<LogisticsPaymentRespDto>>> getLogisticsPaymentStatus(
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "collectionDate", required = false) String collectionDate,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("물류대금마감현황 조회 요청 - 주문번호: {}, 거래처코드: {}, 회수기일: {}, hqCode: {}", 
                orderNo, customerCode, collectionDate, hqCode);
        
        LogisticsPaymentSearchDto searchDto = LogisticsPaymentSearchDto.builder()
                .orderNo(orderNo)
                .customerCode(customerCode)
                .collectionDate(collectionDate)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<LogisticsPaymentRespDto>> response = logisticsPaymentService.getLogisticsPaymentStatus(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}