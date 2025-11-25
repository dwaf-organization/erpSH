package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.brand.reqDto.BrandReqDto;
import com.inc.sh.dto.brand.respDto.BrandRespDto;
import com.inc.sh.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * 브랜드 목록 조회
     * GET /api/v1/erp/brand/list?hq_code=1&brand_code=0
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<BrandRespDto>>> getBrandList(
            @RequestParam("hq_code") Integer hqCode,
            @RequestParam("brand_code") Integer brandCode) {
        
        return ResponseEntity.ok(brandService.getBrandList(hqCode, brandCode));
    }

    /**
     * 브랜드 저장 (신규/수정)
     * POST /api/v1/erp/brand/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<BrandRespDto>> saveBrand(
            @Valid @RequestBody BrandReqDto request) {
        
        return ResponseEntity.ok(brandService.saveBrand(request));
    }

    /**
     * 브랜드 삭제
     * DELETE /api/v1/erp/brand/{brand_code}
     */
    @DeleteMapping("/{brand_code}")
    public ResponseEntity<RespDto<Void>> deleteBrand(
            @PathVariable("brand_code") Integer brandCode) {
        
        return ResponseEntity.ok(brandService.deleteBrand(brandCode));
    }
}