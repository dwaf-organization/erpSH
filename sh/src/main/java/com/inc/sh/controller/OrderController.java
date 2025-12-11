package com.inc.sh.controller;

import com.inc.sh.dto.order.reqDto.*;
import com.inc.sh.dto.order.respDto.*;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.List;
import java.util.stream.Collectors;

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
            @RequestParam(value = "deliveryStatus", required = false) String deliveryStatus,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("주문 목록 조회 요청 - orderDtStart: {}, orderDtEnd: {}, customerName: {}, deliveryStatus: {}, hqCode: {}", 
                orderDtStart, orderDtEnd, customerName, deliveryStatus, hqCode);
        
        OrderSearchDto searchDto = OrderSearchDto.builder()
                .orderDtStart(orderDtStart)
                .orderDtEnd(orderDtEnd)
                .customerName(customerName)
                .deliveryStatus(deliveryStatus)
                .hqCode(hqCode)
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
     * 주문 다중 저장 (신규/수정) - 모든 검증 포함
     * POST /api/v1/erp/order/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<OrderBatchResult>> saveOrders(@RequestBody OrderSaveReqDto request) {
        
        log.info("주문 다중 저장 요청 - 총 {}건", 
                request.getOrders() != null ? request.getOrders().size() : 0);
        
        // 요청 데이터 검증
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 주문 데이터가 없습니다."));
        }
        
        // 개별 항목 필수 필드 검증
        for (OrderSaveReqDto.OrderSaveItemDto order : request.getOrders()) {
            if (order.getHqCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("본사코드는 필수입니다."));
            }
            if (order.getCustomerCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("거래처코드는 필수입니다."));
            }
            if (order.getOrderDt() == null || order.getOrderDt().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("주문일자는 필수입니다."));
            }
            if (order.getDeliveryRequestDt() == null || order.getDeliveryRequestDt().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("납기요청일은 필수입니다."));
            }
            
            // 날짜 형식 검증 (yyyyMMdd)
            try {
                LocalDate.parse(order.getOrderDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate.parse(order.getDeliveryRequestDt(), DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (Exception e) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("날짜 형식이 올바르지 않습니다. (yyyyMMdd 형식)"));
            }
            
            // 배송중 상태일 때 차량코드 필수
            if ("배송중".equals(order.getDeliveryStatus()) && order.getVehicleCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("배송중 상태일 때는 차량코드가 필수입니다."));
            }
            
            // 기본값 설정
            if (order.getDeliveryStatus() == null || order.getDeliveryStatus().trim().isEmpty()) {
                order.setDeliveryStatus("배송요청");
            }
            if (order.getDeliveryAmt() == null) {
                order.setDeliveryAmt(0);
            }
        }
        
        RespDto<OrderBatchResult> response = orderService.saveOrders(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 다중 삭제 (Hard Delete + 검증 포함)
     * DELETE /api/v1/erp/order/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<OrderBatchResult>> deleteOrders(@RequestBody OrderDeleteReqDto request) {
        
        log.info("주문 다중 삭제 요청 - 총 {}건", 
                request.getOrderNos() != null ? request.getOrderNos().size() : 0);
        
        // 요청 데이터 검증
        if (request.getOrderNos() == null || request.getOrderNos().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 주문번호가 없습니다."));
        }
        
        // 주문번호 형식 검증 및 중복 제거
        List<String> validOrderNos = request.getOrderNos().stream()
                .filter(orderNo -> orderNo != null && !orderNo.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        if (validOrderNos.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 주문번호가 없습니다."));
        }
        
        if (validOrderNos.size() != request.getOrderNos().size()) {
            log.info("중복/빈값 주문번호 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getOrderNos().size(), validOrderNos.size());
            request.setOrderNos(validOrderNos);
        }
        
        RespDto<OrderBatchResult> response = orderService.deleteOrders(request);
        return ResponseEntity.ok(response);
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
    
    /**
     * 주문 다중 결제완료 처리
     * PUT /api/v1/erp/order/payment-complete
     */
    @PutMapping("/payment-complete")
    public ResponseEntity<RespDto<OrderPaymentBatchResult>> completePayments(@RequestBody OrderPaymentCompleteReqDto request) {
        
        log.info("주문 다중 결제완료 처리 요청 - 총 {}건", 
                request.getOrderNos() != null ? request.getOrderNos().size() : 0);
        
        // 요청 데이터 검증
        if (request.getOrderNos() == null || request.getOrderNos().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("결제완료 처리할 주문번호가 없습니다."));
        }
        
        // 중복 제거 및 null/빈값 제거
        List<String> validOrderNos = request.getOrderNos().stream()
                .filter(Objects::nonNull)
                .filter(orderNo -> !orderNo.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
        
        if (validOrderNos.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("유효한 주문번호가 없습니다."));
        }
        
        if (validOrderNos.size() != request.getOrderNos().size()) {
            log.info("중복/null 주문번호 제거됨 - 원본: {}건, 제거 후: {}건", 
                    request.getOrderNos().size(), validOrderNos.size());
            request.setOrderNos(validOrderNos);
        }
        
        RespDto<OrderPaymentBatchResult> response = orderService.completePayments(request);
        return ResponseEntity.ok(response);
    }
}