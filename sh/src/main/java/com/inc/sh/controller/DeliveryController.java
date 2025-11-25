package com.inc.sh.controller;

import com.inc.sh.dto.delivery.reqDto.*;
import com.inc.sh.dto.order.respDto.*;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/delivery")
@RequiredArgsConstructor
@Slf4j
public class DeliveryController {

    private final DeliveryService deliveryService;

    /**
     * 배송 주문 목록 조회
     * GET /api/v1/erp/delivery/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<OrderRespDto>>> getDeliveryList(
            @RequestParam(value = "deliveryRequestDt", required = false) String deliveryRequestDt,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "orderNo", required = false) String orderNo,
            @RequestParam(value = "deliveryStatus", required = false) String deliveryStatus) {
        
        log.info("배송 주문 목록 조회 요청 - deliveryRequestDt: {}, customerCode: {}, orderNo: {}, deliveryStatus: {}", 
                deliveryRequestDt, customerCode, orderNo, deliveryStatus);
        
        DeliverySearchDto searchDto = DeliverySearchDto.builder()
                .deliveryRequestDt(deliveryRequestDt)
                .customerCode(customerCode)
                .orderNo(orderNo)
                .deliveryStatus(deliveryStatus)
                .build();
        
        RespDto<List<OrderRespDto>> response = deliveryService.getDeliveryList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 배송 주문품목 목록 조회
     * GET /api/v1/erp/delivery/item-list/{orderNo}
     */
    @GetMapping("/item-list/{orderNo}")
    public ResponseEntity<RespDto<List<OrderItemRespDto>>> getDeliveryItemList(
            @PathVariable("orderNo") String orderNo) {
        
        log.info("배송 주문품목 목록 조회 요청 - orderNo: {}", orderNo);
        
        RespDto<List<OrderItemRespDto>> response = deliveryService.getDeliveryItemList(orderNo);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 배송시작 (배송요청 → 배송중)
     * POST /api/v1/erp/delivery/start
     */
    @PostMapping("/start")
    public ResponseEntity<RespDto<List<String>>> startDelivery(@RequestBody DeliveryStartDto startDto) {
        
        log.info("배송시작 요청 - 주문수: {}", startDto.getOrders() != null ? startDto.getOrders().size() : 0);
        
        RespDto<List<String>> response = deliveryService.startDelivery(startDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 배송취소 (배송중 → 배송요청)
     * POST /api/v1/erp/delivery/cancel
     */
    @PostMapping("/cancel")
    public ResponseEntity<RespDto<List<String>>> cancelDelivery(@RequestBody DeliveryCancelDto cancelDto) {
        
        log.info("배송취소 요청 - 주문수: {}", cancelDto.getOrderNos() != null ? cancelDto.getOrderNos().size() : 0);
        
        RespDto<List<String>> response = deliveryService.cancelDelivery(cancelDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 배송완료 (배송요청/배송중 → 배송완료)
     * POST /api/v1/erp/delivery/complete
     */
    @PostMapping("/complete")
    public ResponseEntity<RespDto<List<String>>> completeDelivery(@RequestBody DeliveryCompleteDto completeDto) {
        
        log.info("배송완료 요청 - 주문수: {}", completeDto.getOrderNos() != null ? completeDto.getOrderNos().size() : 0);
        
        RespDto<List<String>> response = deliveryService.completeDelivery(completeDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}