package com.inc.sh.controller;

import com.inc.sh.dto.itemCategory.reqDto.ItemCategoryReqDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTreeRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryTableRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategorySaveRespDto;
import com.inc.sh.dto.itemCategory.respDto.ItemCategoryDeleteRespDto;
import com.inc.sh.service.ItemCategoryService;
import jakarta.validation.Valid;
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
    public ResponseEntity<RespDto<List<ItemCategoryTreeRespDto>>> getCategoryTreeList() {
        
        log.info("트리용 품목분류 전체 조회 요청");
        
        RespDto<List<ItemCategoryTreeRespDto>> response = itemCategoryService.getCategoryTreeList();
        
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
    public ResponseEntity<RespDto<List<ItemCategoryTableRespDto>>> getCategoryTableList() {
        
        log.info("표용 품목분류 전체 조회 요청");
        
        RespDto<List<ItemCategoryTableRespDto>> response = itemCategoryService.getCategoryTableList();
        
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
     * 품목분류 저장 (신규/수정)
     * POST /api/v1/erp/item-category/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<ItemCategorySaveRespDto>> saveCategory(
            @Valid @RequestBody ItemCategoryReqDto request) {
        
        if (request.getCategoryCode() == null) {
            log.info("품목분류 신규 등록 요청 - categoryName: {}, parentsCategoryCode: {}", 
                    request.getCategoryName(), request.getParentsCategoryCode());
        } else {
            log.info("품목분류 수정 요청 - categoryCode: {}, categoryName: {}", 
                    request.getCategoryCode(), request.getCategoryName());
        }
        
        RespDto<ItemCategorySaveRespDto> response = itemCategoryService.saveCategory(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 품목분류 삭제 (하드 삭제)
     * DELETE /api/v1/erp/item-category/{categoryCode}
     */
    @DeleteMapping("/{categoryCode}")
    public ResponseEntity<RespDto<ItemCategoryDeleteRespDto>> deleteCategory(
            @PathVariable("categoryCode") Integer categoryCode) {
        
        log.info("품목분류 삭제 요청 - categoryCode: {}", categoryCode);
        
        RespDto<ItemCategoryDeleteRespDto> response = itemCategoryService.deleteCategory(categoryCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}