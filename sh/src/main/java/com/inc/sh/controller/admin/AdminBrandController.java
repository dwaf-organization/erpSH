package com.inc.sh.controller.admin;

import com.inc.sh.dto.brand.respDto.AdminBrandListRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.admin.AdminBrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
@Slf4j
public class AdminBrandController {

    private final AdminBrandService adminBrandService;

    /**
     * 관리자 브랜드 목록 조회
     * GET /api/v1/admin/brands/list?hqCode={hqCode}
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<AdminBrandListRespDto>>> getBrandList(
            @RequestParam(value = "hqCode", required = false) Integer hqCode) {
        
        String searchType = (hqCode == null) ? "전체 조회" : "본사별 조회";
        log.info("관리자 브랜드 목록 조회 요청 - {}, hqCode: {}", searchType, hqCode);
        
        RespDto<List<AdminBrandListRespDto>> response = adminBrandService.getBrandList(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}