package com.inc.sh.controller;

import com.inc.sh.dto.picking.reqDto.PickingListSearchDto;
import com.inc.sh.dto.picking.respDto.PickingListRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.PickingListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/picking-list")
@RequiredArgsConstructor
@Slf4j
public class PickingListController {

    private final PickingListService pickingListService;

    /**
     * 품목별 PickingList 조회
     * GET /api/v1/erp/picking-list/item
     */
    @GetMapping("/item")
    public ResponseEntity<RespDto<List<PickingListRespDto>>> getPickingList(
            @RequestParam(value = "deliveryRequestDtStart", required = false) String deliveryRequestDtStart,
            @RequestParam(value = "deliveryRequestDtEnd", required = false) String deliveryRequestDtEnd,
            @RequestParam(value = "itemCode", required = false) Integer itemCode,
            @RequestParam(value = "categoryCode", required = false) Integer categoryCode,
            @RequestParam(value = "customerCode", required = false) Integer customerCode,
            @RequestParam(value = "distCenterCode", required = false) Integer distCenterCode,
            @RequestParam(value = "brandCode", required = false) Integer brandCode) {
        
        log.info("품목별 PickingList 조회 요청 - 납기일자: {}~{}, 품목코드: {}, 분류코드: {}, 거래처코드: {}, 물류센터코드: {}, 브랜드코드: {}", 
                deliveryRequestDtStart, deliveryRequestDtEnd, itemCode, categoryCode, customerCode, distCenterCode, brandCode);
        
        PickingListSearchDto searchDto = PickingListSearchDto.builder()
                .deliveryRequestDtStart(deliveryRequestDtStart)
                .deliveryRequestDtEnd(deliveryRequestDtEnd)
                .itemCode(itemCode)
                .categoryCode(categoryCode)
                .customerCode(customerCode)
                .distCenterCode(distCenterCode)
                .brandCode(brandCode)
                .build();
        
        RespDto<List<PickingListRespDto>> response = pickingListService.getPickingList(searchDto);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}