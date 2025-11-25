package com.inc.sh.controller;

import com.inc.sh.dto.order.reqDto.*;
import com.inc.sh.dto.order.respDto.*;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 목록 조회
     * GET /api/v1/erp/order/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<OrderRespDto>>> getOrderList(
            @RequestParam(value = "orderDtStart", required = false) String orderDtStart,
            @RequestParam(value = "orderDtEnd", required = false) String orderDtEnd,
            @RequestParam(value = "customerName", required = false) String customerName,
            @RequestParam(value = "deliveryStatus", required = false) String deliveryStatus) {
        
        log.info("주문 목록 조회 요청 - orderDtStart: {}, orderDtEnd: {}, customerName: {}, deliveryStatus: {}", 
                orderDtStart, orderDtEnd, customerName, deliveryStatus);
        
        OrderSearchDto searchDto = OrderSearchDto.builder()
                .orderDtStart(orderDtStart)
                .orderDtEnd(orderDtEnd)
                .customerName(customerName)
                .deliveryStatus(deliveryStatus)
                .build();
        
        RespDto<List<OrderRespDto>> response = orderService.getOrderList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문품목 목록 조회
     * GET /api/v1/erp/order/item-list?orderNo=20241112-0001
     */
    @GetMapping("/item-list")
    public ResponseEntity<RespDto<List<OrderItemRespDto>>> getOrderItemList(
            @RequestParam("orderNo") String orderNo) {
        
        log.info("주문품목 목록 조회 요청 - orderNo: {}", orderNo);
        
        RespDto<List<OrderItemRespDto>> response = orderService.getOrderItemList(orderNo);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문 저장 (신규/수정)
     * POST /api/v1/erp/order/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<String>> saveOrder(@RequestBody OrderSaveDto saveDto) {
        
        log.info("주문 저장 요청 - orderNo: {}, customerCode: {}", saveDto.getOrderNo(), saveDto.getCustomerCode());
        
        RespDto<String> response = orderService.saveOrder(saveDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문품목 업데이트 (품목 저장 + 주문 금액 업데이트)
     * POST /api/v1/erp/order/item-update
     */
    @PostMapping("/item-update")
    public ResponseEntity<RespDto<String>> updateOrderItems(@RequestBody OrderItemUpdateDto updateDto) {
        
        log.info("주문품목 업데이트 요청 - orderNo: {}, 품목수: {}", updateDto.getOrderNo(), 
                updateDto.getOrderItems() != null ? updateDto.getOrderItems().size() : 0);
        
        RespDto<String> response = orderService.updateOrderItems(updateDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문 삭제
     * DELETE /api/v1/erp/order/{orderNo}
     */
    @DeleteMapping("/{orderNo}")
    public ResponseEntity<RespDto<String>> deleteOrder(@PathVariable("orderNo") String orderNo) {
        
        log.info("주문 삭제 요청 - orderNo: {}", orderNo);
        
        RespDto<String> response = orderService.deleteOrder(orderNo);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 주문품목 삭제 (개별 품목 삭제)
     * DELETE /api/v1/erp/order/item/{orderItemCode}
     */
    @DeleteMapping("/item/{orderItemCode}")
    public ResponseEntity<RespDto<String>> deleteOrderItem(@PathVariable("orderItemCode") Integer orderItemCode) {
        
        log.info("주문품목 삭제 요청 - orderItemCode: {}", orderItemCode);
        
        RespDto<String> response = orderService.deleteOrderItem(orderItemCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}