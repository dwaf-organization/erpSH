package com.inc.sh.controller;

import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceSearchDto;
import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceBatchResult;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceRespDto;
import com.inc.sh.dto.deliveryPrice.respDto.DeliveryPriceUpdateRespDto;
import com.inc.sh.service.DeliveryPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/delivery-price")
@RequiredArgsConstructor
@Slf4j
public class DeliveryPriceController {

    private final DeliveryPriceService deliveryPriceService;

    /**
     * 납품단가관리용 품목 조회
     * GET /api/v1/erp/delivery-price/items
     */
    @GetMapping("/items")
    public ResponseEntity<RespDto<List<DeliveryPriceRespDto>>> getItemsForPriceManagement(
            @RequestParam(value = "itemCode", required = false) Integer itemCode,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "priceType", required = false) Integer priceType,
            @RequestParam(value = "hqCode") Integer hqCode) {
        
        log.info("납품단가관리 품목 조회 요청 - itemCode: {}, categoryCode: {}, priceType: {}, hqCode: {}", 
                itemCode, categoryCode, priceType, hqCode);
        
        DeliveryPriceSearchDto searchDto = DeliveryPriceSearchDto.builder()
                .itemCode(itemCode)
                .categoryCode(categoryCode)
                .priceType(priceType)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<DeliveryPriceRespDto>> response = deliveryPriceService.getItemsForPriceManagement(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 납품단가 다중 수정
     * PUT /api/v1/erp/delivery-price/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<DeliveryPriceBatchResult>> updateDeliveryPrices(@RequestBody DeliveryPriceUpdateReqDto request) {
        
        log.info("납품단가 다중 수정 요청 - 총 {}건", 
                request.getItems() != null ? request.getItems().size() : 0);
        
        // 요청 데이터 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("수정할 납품단가 데이터가 없습니다."));
        }
        
        // 개별 항목 검증
        for (DeliveryPriceUpdateReqDto.DeliveryPriceItemDto item : request.getItems()) {
            if (item.getItemCode() == null) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("품목코드는 필수입니다."));
            }
            if (item.getBasePrice() == null || item.getBasePrice() <= 0) {
                return ResponseEntity.badRequest()
                        .body(RespDto.fail("기본단가는 필수이며 0보다 커야 합니다."));
            }
        }
        
        RespDto<DeliveryPriceBatchResult> response = deliveryPriceService.updateDeliveryPrices(request);
        return ResponseEntity.ok(response);
    }
}