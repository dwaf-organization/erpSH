package com.inc.sh.controller;

import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceSearchDto;
import com.inc.sh.dto.deliveryPrice.reqDto.DeliveryPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
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
            @RequestParam(value = "priceType", required = false) Integer priceType) {
        
        log.info("납품단가관리 품목 조회 요청 - itemCode: {}, categoryCode: {}, priceType: {}", 
                itemCode, categoryCode, priceType);
        
        DeliveryPriceSearchDto searchDto = DeliveryPriceSearchDto.builder()
                .itemCode(itemCode)
                .categoryCode(categoryCode)
                .priceType(priceType)
                .build();
        
        RespDto<List<DeliveryPriceRespDto>> response = deliveryPriceService.getItemsForPriceManagement(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목 단가 수정
     * PUT /api/v1/erp/delivery-price/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<DeliveryPriceUpdateRespDto>> updateItemPrice(
            @Valid @RequestBody DeliveryPriceUpdateReqDto request) {
        
        log.info("품목 단가 수정 요청 - itemCode: {}, basePrice: {}", 
                request.getItemCode(), request.getBasePrice());
        
        RespDto<DeliveryPriceUpdateRespDto> response = deliveryPriceService.updateItemPrice(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}