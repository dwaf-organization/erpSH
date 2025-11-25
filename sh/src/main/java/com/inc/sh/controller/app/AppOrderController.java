package com.inc.sh.controller.app;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.order.reqDto.OrderCreateReqDto;
import com.inc.sh.dto.order.reqDto.AppOrderConfirmReqDto;
import com.inc.sh.dto.order.respDto.AppOrderItemListRespDto;
import com.inc.sh.dto.order.respDto.AppOrderRespDto;
import com.inc.sh.dto.order.respDto.AppOrderHistoryRespDto;
import com.inc.sh.dto.order.respDto.AppOrderDetailRespDto;
import com.inc.sh.dto.order.respDto.AppOrderConfirmRespDto;
import com.inc.sh.dto.order.respDto.AppAccountInfoRespDto;
import com.inc.sh.service.app.AppOrderService;
import com.inc.sh.service.app.AppOrderCreateService;
import com.inc.sh.service.app.AppOrderHistoryService;
import com.inc.sh.service.app.AppOrderDetailService;
import com.inc.sh.service.app.AppCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/app/order")
@RequiredArgsConstructor
@Slf4j
public class AppOrderController {
    
    private final AppOrderService appOrderService;
    private final AppOrderCreateService appOrderCreateService;
    private final AppOrderHistoryService appOrderHistoryService;
    private final AppOrderDetailService appOrderDetailService;
    private final AppCustomerService appCustomerService;
    
    /**
     * [앱전용] 주문가능품목 조회
     * GET /api/v1/app/order/item-list
     */
    @GetMapping("/item-list")
    public ResponseEntity<RespDto<List<AppOrderItemListRespDto>>> getOrderableItemList(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("customerUserCode") Integer customerUserCode,
            @RequestParam("itemType") String itemType,
            @RequestParam(value = "categoryCode", required = false) String categoryCode,
            @RequestParam(value = "itemName", required = false) String itemName) {
        
        log.info("[앱] 주문가능품목 조회 요청 - customerCode: {}, itemType: {}, categoryCode: {}, itemName: {}", 
                customerCode, itemType, categoryCode, itemName);
        
        RespDto<List<AppOrderItemListRespDto>> response = appOrderService.findOrderableItemsForApp(
                customerCode, customerUserCode, itemType, categoryCode, itemName);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 주문 생성
     * POST /api/v1/app/order/create
     */
    @PostMapping("/create")
    public ResponseEntity<RespDto<AppOrderRespDto>> createOrder(@RequestBody OrderCreateReqDto request) {
        
        log.info("[앱] 주문 생성 요청 - customerCode: {}, deliveryRequestDt: {}, totalAmt: {}", 
                request.getCustomerCode(), request.getDeliveryRequestDt(), request.getTotalAmt());
        
        RespDto<AppOrderRespDto> response = appOrderCreateService.createOrder(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 주문내역 조회
     * GET /api/v1/app/order/history
     */
    @GetMapping("/history")
    public ResponseEntity<RespDto<AppOrderHistoryRespDto>> getOrderHistory(
            @RequestParam("customerCode") Integer customerCode,
            @RequestParam("deliveryRequestStartDt") String deliveryRequestStartDt,
            @RequestParam("deliveryRequestEndDt") String deliveryRequestEndDt,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        log.info("[앱] 주문내역 조회 - customerCode: {}, 기간: {}~{}, page: {}, size: {}", 
                customerCode, deliveryRequestStartDt, deliveryRequestEndDt, page, size);
        
        RespDto<AppOrderHistoryRespDto> response = appOrderHistoryService.getOrderHistory(
                customerCode, deliveryRequestStartDt, deliveryRequestEndDt, page, size);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 주문상세 조회
     * GET /api/v1/app/order/detail
     */
    @GetMapping("/detail")
    public ResponseEntity<RespDto<AppOrderDetailRespDto>> getOrderDetail(
            @RequestParam("orderNo") String orderNo,
            @RequestParam("customerCode") Integer customerCode) {
        
        log.info("[앱] 주문상세 조회 - orderNo: {}, customerCode: {}", orderNo, customerCode);
        
        RespDto<AppOrderDetailRespDto> response = appOrderDetailService.getOrderDetail(orderNo, customerCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 수령확인
     * POST /api/v1/app/order/confirm-delivery
     */
    @PostMapping("/confirm-delivery")
    public ResponseEntity<RespDto<AppOrderConfirmRespDto>> confirmDelivery(@RequestBody AppOrderConfirmReqDto request) {
        
        log.info("[앱] 수령확인 요청 - orderNo: {}, customerCode: {}", request.getOrderNo(), request.getCustomerCode());
        
        RespDto<AppOrderConfirmRespDto> response = appOrderDetailService.confirmDelivery(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * [앱전용] 가상계좌 및 잔액 조회
     * GET /api/v1/app/order/account-info
     */
    @GetMapping("/account-info")
    public ResponseEntity<RespDto<AppAccountInfoRespDto>> getAccountInfo(
            @RequestParam("customerCode") Integer customerCode) {
        
        log.info("[앱] 가상계좌 정보 조회 - customerCode: {}", customerCode);
        
        RespDto<AppAccountInfoRespDto> response = appCustomerService.getAccountInfo(customerCode);
        
        return ResponseEntity.ok(response);
    }
}