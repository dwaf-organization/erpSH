package com.inc.sh.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.headquarter.reqDto.SiteInfoReqDto;
import com.inc.sh.dto.headquarter.respDto.SiteInfoRespDto;
import com.inc.sh.service.HeadquarterService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/erp/site-info")
@RequiredArgsConstructor
@Slf4j
public class SiteInfoController {

    private final HeadquarterService headquarterService;

    /**
     * 본사 정보 조회 (본사코드 직접 지정) - 개발/테스트용
     * GET /api/v1/erp/site-info/detail/{hqCode}
     */
    @GetMapping("/detail/{hqCode}")
    public ResponseEntity<RespDto<SiteInfoRespDto>> getSiteInfoByCode(
            @PathVariable("hqCode") Integer hqCode) {
        log.info("본사 정보 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<SiteInfoRespDto> response = headquarterService.getSiteInfo(hqCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 본사 정보 수정
     * POST /api/v1/erp/site-info/update
     */
    @PostMapping("/update")
    public ResponseEntity<RespDto<SiteInfoRespDto>> updateSiteInfo(
            @Valid @RequestBody SiteInfoReqDto request) {
        log.info("본사 정보 수정 요청 - hqCode: {}, companyName: {}", 
                request.getHqCode(), request.getCompanyName());
        
        RespDto<SiteInfoRespDto> response = headquarterService.updateSiteInfo(request);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
