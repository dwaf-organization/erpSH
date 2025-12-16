package com.inc.sh.controller;

import com.inc.sh.dto.analysisTest.respDto.AnalysisTestRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.AnalysisTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analysisTest")
@RequiredArgsConstructor
@Slf4j
public class AnalysisTestController {

    private final AnalysisTestService analysisTestService;

    /**
     * 구코드로 상권분석 조회
     * GET /api/v1/analysisTest/district?districtCode={districtCode}
     */
    @GetMapping("/district")
    public ResponseEntity<RespDto<AnalysisTestRespDto>> getDistrictAnalysis(
            @RequestParam("districtCode") Integer districtCode) {
        
        log.info("상권분석 조회 요청 - districtCode: {}", districtCode);
        
        RespDto<AnalysisTestRespDto> response = analysisTestService.getDistrictAnalysis(districtCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 전체 상권분석 목록 조회
     * GET /api/v1/analysisTest/districts
     */
    @GetMapping("/districts")
    public ResponseEntity<RespDto<List<AnalysisTestRespDto>>> getAllDistrictAnalysis() {
        
        log.info("전체 상권분석 목록 조회 요청");
        
        RespDto<List<AnalysisTestRespDto>> response = analysisTestService.getAllDistrictAnalysis();
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 성장 구역별 상권분석 조회 (상승/하락)
     * GET /api/v1/analysisTest/growth?growthCode={growthCode}
     */
    @GetMapping("/growth")
    public ResponseEntity<RespDto<List<AnalysisTestRespDto>>> getDistrictAnalysisByGrowth(
            @RequestParam("growthCode") Integer growthCode) {
        
        String growthType = growthCode == 1 ? "상승" : "하락";
        log.info("성장 구역별 상권분석 조회 요청 - growthCode: {} ({})", growthCode, growthType);
        
        RespDto<List<AnalysisTestRespDto>> response = analysisTestService.getDistrictAnalysisByGrowth(growthCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}