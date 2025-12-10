package com.inc.sh.controller;

import com.inc.sh.dto.item.reqDto.ItemDeleteReqDto;
import com.inc.sh.dto.item.reqDto.ItemReqDto;
import com.inc.sh.dto.item.reqDto.ItemSaveReqDto;
import com.inc.sh.dto.item.reqDto.ItemSearchDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.item.respDto.ItemRespDto;
import com.inc.sh.dto.item.respDto.ItemSaveRespDto;
import com.inc.sh.dto.item.respDto.ItemBatchResult;
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
     * 품목 목록 조회 (수정됨 - item 통합검색)
     * GET /api/v1/erp/item/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<ItemRespDto>>> getItemList(
            @RequestParam(value = "item", required = false) String item,
            @RequestParam(value = "categoryCode", required = false) String categoryCode,
            @RequestParam(value = "priceType", required = false) String priceType,
            @RequestParam(value = "endDtYn", required = false) String endDtYn,
            @RequestParam(value = "orderAvailableYn", required = false) String orderAvailableYn,
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("품목 목록 조회 요청 - item: {}, categoryCode: {}, priceType: {}, endDtYn: {}, orderAvailableYn: {}, hqCode: {}", 
                item, categoryCode, priceType, endDtYn, orderAvailableYn, hqCode);
        
        RespDto<List<ItemRespDto>> response = itemService.getItemList(
                item, categoryCode, priceType, endDtYn, orderAvailableYn, hqCode);
        
        return ResponseEntity.ok(response);
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
     * 품목 다중 저장 (신규/수정)
     * POST /api/v1/erp/item/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<ItemBatchResult>> saveItems(@RequestBody ItemSaveReqDto request) {
        
        log.info("품목 다중 저장 요청 - 총 {}건", 
                request.getItems() != null ? request.getItems().size() : 0);
        
        // 요청 데이터 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 품목 데이터가 없습니다."));
        }
        
        RespDto<ItemBatchResult> response = itemService.saveItems(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 품목 다중 삭제 (Soft Delete)
     * DELETE /api/v1/erp/item/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<ItemBatchResult>> deleteItems(@RequestBody ItemDeleteReqDto request) {
        
        log.info("품목 다중 삭제 요청 - 총 {}건", 
                request.getItemCodes() != null ? request.getItemCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getItemCodes() == null || request.getItemCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 품목 코드가 없습니다."));
        }
        
        RespDto<ItemBatchResult> response = itemService.deleteItems(request);
        return ResponseEntity.ok(response);
    }
}