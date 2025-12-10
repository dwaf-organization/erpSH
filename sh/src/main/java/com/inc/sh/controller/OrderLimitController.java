package com.inc.sh.controller;

import com.inc.sh.dto.orderLimit.reqDto.OrderLimitSearchDto;
import com.inc.sh.dto.orderLimit.reqDto.OrderLimitUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.orderLimit.respDto.ItemForOrderLimitRespDto;
import com.inc.sh.dto.orderLimit.respDto.CustomerLimitRespDto;
import com.inc.sh.dto.orderLimit.respDto.OrderLimitUpdateRespDto;
import com.inc.sh.service.OrderLimitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/order-limit")
@RequiredArgsConstructor
@Slf4j
public class OrderLimitController {

    private final OrderLimitService orderLimitService;

    /**
     * 주문제한 설정용 품목 목록 조회
     * GET /api/v1/erp/order-limit/item-list
     */
    @GetMapping("/item-list")
    public ResponseEntity<RespDto<List<ItemForOrderLimitRespDto>>> getItemListForOrderLimit(
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("주문제한 설정 품목 조회 요청 - categoryCode: {}, itemName: {}, hqCode: {}", categoryCode, itemName, hqCode);
        
        OrderLimitSearchDto searchDto = OrderLimitSearchDto.builder()
                .categoryCode(categoryCode)
                .itemName(itemName)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<ItemForOrderLimitRespDto>> response = orderLimitService.getItemListForOrderLimit(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목별 거래처 제한 정보 조회
     * GET /api/v1/erp/order-limit/customer-list
     */
    @GetMapping("/customer-list")
    public ResponseEntity<RespDto<List<CustomerLimitRespDto>>> getCustomerLimitsByItem(
            @RequestParam("itemCode") Integer itemCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("품목별 거래처 제한 정보 조회 요청 - itemCode: {}, hqCode: {}", itemCode, hqCode);
        
        RespDto<List<CustomerLimitRespDto>> response = orderLimitService.getCustomerLimitsByItem(itemCode, hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문제한 일괄 수정
     * PUT /api/v1/erp/order-limit/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<OrderLimitUpdateRespDto>> updateOrderLimits(
            @Valid @RequestBody OrderLimitUpdateReqDto request) {
        
        log.info("주문제한 일괄 수정 요청 - itemCode: {}, 거래처 수: {}", 
                request.getItemCode(), request.getCustomerLimits().size());
        
        RespDto<OrderLimitUpdateRespDto> response = orderLimitService.updateOrderLimits(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}