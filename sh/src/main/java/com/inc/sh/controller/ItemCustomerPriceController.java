package com.inc.sh.controller;

import com.inc.sh.dto.itemCustomerPrice.reqDto.ItemCustomerPriceSearchDto;
import com.inc.sh.dto.itemCustomerPrice.reqDto.ItemCustomerPriceUpdateReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.ItemForCustomerPriceRespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.CustomerPriceRespDto;
import com.inc.sh.dto.itemCustomerPrice.respDto.ItemCustomerPriceUpdateRespDto;
import com.inc.sh.service.ItemCustomerPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/item-customer-price")
@RequiredArgsConstructor
@Slf4j
public class ItemCustomerPriceController {

    private final ItemCustomerPriceService itemCustomerPriceService;

    /**
     * 거래처별 단가관리용 품목 목록 조회
     * GET /api/v1/erp/item-customer-price/item-list
     */
    @GetMapping("/item-list")
    public ResponseEntity<RespDto<List<ItemForCustomerPriceRespDto>>> getItemListForCustomerPrice(
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("거래처별 단가관리 품목 조회 요청 - categoryCode: {}, itemName: {}, hqCode: {}", categoryCode, itemName, hqCode);
        
        ItemCustomerPriceSearchDto searchDto = ItemCustomerPriceSearchDto.builder()
                .categoryCode(categoryCode)
                .itemName(itemName)
                .hqCode(hqCode)
                .build();
        
        RespDto<List<ItemForCustomerPriceRespDto>> response = itemCustomerPriceService.getItemListForCustomerPrice(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목별 거래처 단가 조회
     * GET /api/v1/erp/item-customer-price/customer-list
     */
    @GetMapping("/customer-list")
    public ResponseEntity<RespDto<List<CustomerPriceRespDto>>> getCustomerPricesByItem(
            @RequestParam("itemCode") Integer itemCode,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("품목별 거래처 단가 조회 요청 - itemCode: {}, hqCode: {}", itemCode, hqCode);
        
        RespDto<List<CustomerPriceRespDto>> response = itemCustomerPriceService.getCustomerPricesByItem(itemCode, hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 거래처별 단가 일괄 수정
     * PUT /api/v1/erp/item-customer-price/update
     */
    @PutMapping("/update")
    public ResponseEntity<RespDto<ItemCustomerPriceUpdateRespDto>> updateCustomerPrices(
            @Valid @RequestBody ItemCustomerPriceUpdateReqDto request) {
        
        log.info("거래처별 단가 일괄 수정 요청 - itemCode: {}, 거래처 수: {}", 
                request.getItemCode(), request.getCustomerPrices().size());
        
        RespDto<ItemCustomerPriceUpdateRespDto> response = itemCustomerPriceService.updateCustomerPrices(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}