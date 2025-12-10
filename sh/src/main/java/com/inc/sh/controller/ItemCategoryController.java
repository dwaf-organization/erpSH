package com.inc.sh.controller;

import com.inc.sh.dto.itemCategory.reqDto.ItemCategoryDeleteReqDto;
import com.inc.sh.dto.itemCategory.reqDto.ItemCategorySaveReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTreeRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTableRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryBatchResult;
import com.inc.sh.service.ItemCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/item-category")
@RequiredArgsConstructor
@Slf4j
public class ItemCategoryController {

    private final ItemCategoryService itemCategoryService;

    /**
     * 트리용 전체 품목분류 조회 (계층구조)
     * GET /api/v1/erp/item-category/tree
     */
    @GetMapping("/tree")
    public ResponseEntity<RespDto<List<ItemCategoryTreeRespDto>>> getCategoryTreeList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("트리용 품목분류 전체 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<ItemCategoryTreeRespDto>> response = itemCategoryService.getCategoryTreeList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 표용 전체 품목분류 조회 (평면 리스트)
     * GET /api/v1/erp/item-category/table
     */
    @GetMapping("/table")
    public ResponseEntity<RespDto<List<ItemCategoryTableRespDto>>> getCategoryTableList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("표용 품목분류 전체 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<ItemCategoryTableRespDto>> response = itemCategoryService.getCategoryTableList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 트리 클릭 시 본인 및 하위 항목 조회
     * GET /api/v1/erp/item-category/children/{categoryCode}
     */
    @GetMapping("/children/{categoryCode}")
    public ResponseEntity<RespDto<List<ItemCategoryTableRespDto>>> getCategoryWithChildren(
            @PathVariable("categoryCode") Integer categoryCode) {
        
        log.info("트리 클릭 조회 요청 - categoryCode: {}", categoryCode);
        
        RespDto<List<ItemCategoryTableRespDto>> response = itemCategoryService.getCategoryWithChildren(categoryCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목분류 다중 저장 (신규/수정)
     * POST /api/v1/erp/item-category/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<ItemCategoryBatchResult>> saveItemCategories(@RequestBody ItemCategorySaveReqDto request) {
        
        log.info("품목분류 다중 저장 요청 - 총 {}건", 
                request.getItemCategories() != null ? request.getItemCategories().size() : 0);
        
        // 요청 데이터 검증
        if (request.getItemCategories() == null || request.getItemCategories().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("저장할 품목분류 데이터가 없습니다."));
        }
        
        RespDto<ItemCategoryBatchResult> response = itemCategoryService.saveItemCategories(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 품목분류 다중 삭제 (Hard Delete)
     * DELETE /api/v1/erp/item-category/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<ItemCategoryBatchResult>> deleteItemCategories(@RequestBody ItemCategoryDeleteReqDto request) {
        
        log.info("품목분류 다중 삭제 요청 - 총 {}건", 
                request.getCategoryCodes() != null ? request.getCategoryCodes().size() : 0);
        
        // 요청 데이터 검증
        if (request.getCategoryCodes() == null || request.getCategoryCodes().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(RespDto.fail("삭제할 품목분류 코드가 없습니다."));
        }
        
        RespDto<ItemCategoryBatchResult> response = itemCategoryService.deleteItemCategories(request);
        return ResponseEntity.ok(response);
    }
}