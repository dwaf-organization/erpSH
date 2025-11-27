package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.brand.reqDto.BrandReqDto;
import com.inc.sh.dto.brand.reqDto.BrandDeleteReqDto;
import com.inc.sh.dto.brand.respDto.BrandRespDto;
import com.inc.sh.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/brand")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

    private final BrandService brandService;

    /**
     * 브랜드 목록 조회
     * GET /api/v1/erp/brand/list
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<BrandRespDto>>> getBrandList(
            @RequestParam("hq_code") Integer hqCode,
            @RequestParam(value = "brand_code", defaultValue = "0") Integer brandCode) {
        
        log.info("브랜드 목록 조회 요청 - hqCode: {}, brandCode: {}", hqCode, brandCode);
        
        RespDto<List<BrandRespDto>> response = brandService.getBrandList(hqCode, brandCode);
        return ResponseEntity.ok(response);
    }

    /**
     * 브랜드 저장 (신규/수정)
     * POST /api/v1/erp/brand/save
     */
    @PostMapping("/save")
    public ResponseEntity<RespDto<BrandRespDto>> saveBrand(@RequestBody BrandReqDto request) {
        
        log.info("브랜드 저장 요청 - brandCode: {}, brandName: {}", 
                request.getBrandCode(), request.getBrandName());
        
        RespDto<BrandRespDto> response = brandService.saveBrand(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 브랜드 다중 삭제
     * DELETE /api/v1/erp/brand/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<RespDto<String>> deleteBrand(@RequestBody BrandDeleteReqDto deleteDto) {
        
        log.info("브랜드 다중 삭제 요청 - 삭제 대상: {} 건", 
                deleteDto.getBrandCodes() != null ? deleteDto.getBrandCodes().size() : 0);
        
        RespDto<String> response = brandService.deleteBrandMultiple(deleteDto);
        return ResponseEntity.ok(response);
    }
}