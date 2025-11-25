package com.inc.sh.controller;

import com.inc.sh.dto.item.reqDto.ItemReqDto;
import com.inc.sh.dto.item.reqDto.ItemSearchDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.item.respDto.ItemSaveRespDto;
import com.inc.sh.dto.item.respDto.ItemDeleteRespDto;
import com.inc.sh.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/item")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * 품목 목록 조회
     * GET /api/v1/erp/item/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<ItemRespDto>>> getItemList(
            @RequestParam(value = "itemCode", required = false) String itemCode,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "priceType", required = false) Integer priceType,
            @RequestParam(value = "endDtYn", required = false) Integer endDtYn,
            @RequestParam(value = "orderAvailableYn", required = false) Integer orderAvailableYn) {
        
        log.info("품목 목록 조회 요청 - itemCode: {}, itemName: {}, categoryCode: {}, priceType: {}, endDtYn: {}, orderAvailableYn: {}", 
                itemCode, itemName, categoryCode, priceType, endDtYn, orderAvailableYn);
        
        ItemSearchDto searchDto = ItemSearchDto.builder()
                .itemCode(itemCode)
                .itemName(itemName)
                .categoryCode(categoryCode)
                .priceType(priceType)
                .endDtYn(endDtYn)
                .orderAvailableYn(orderAvailableYn)
                .build();
        
        RespDto<List<ItemRespDto>> response = itemService.getItemList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목 상세 조회
     * GET /api/v1/erp/item/detail/{itemCode}
     */
    @GetMapping("/detail/{itemCode}")
    public ResponseEntity<RespDto<ItemRespDto>> getItem(
            @PathVariable("itemCode") Integer itemCode) {
        
        log.info("품목 상세 조회 요청 - itemCode: {}", itemCode);
        
        RespDto<ItemRespDto> response = itemService.getItem(itemCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목 저장 (신규/수정)
     * POST /api/v1/erp/item/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<ItemSaveRespDto>> saveItem(
            @Valid @RequestBody ItemReqDto request) {
        
        if (request.getItemCode() == null) {
            log.info("품목 신규 등록 요청 - itemName: {}, categoryCode: {}", 
                    request.getItemName(), request.getCategoryCode());
        } else {
            log.info("품목 수정 요청 - itemCode: {}, itemName: {}", 
                    request.getItemCode(), request.getItemName());
        }
        
        RespDto<ItemSaveRespDto> response = itemService.saveItem(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목 삭제 (소프트 삭제)
     * DELETE /api/v1/erp/item/{itemCode}
     */
    @DeleteMapping("/{itemCode}")
    public ResponseEntity<RespDto<ItemDeleteRespDto>> deleteItem(
            @PathVariable("itemCode") Integer itemCode) {
        
        log.info("품목 삭제 요청 - itemCode: {}", itemCode);
        
        RespDto<ItemDeleteRespDto> response = itemService.deleteItem(itemCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}