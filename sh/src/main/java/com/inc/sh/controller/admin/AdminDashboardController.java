package com.inc.sh.controller.admin;

import com.inc.sh.dto.dashboard.respDto.AdminDashboardOverviewRespDto;
import com.inc.sh.dto.dashboard.respDto.AdminHeadquartersStatsRespDto;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    /**
     * 관리자 대시보드 현황 조회
     * GET /api/v1/admin/dashboard/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<RespDto<AdminDashboardOverviewRespDto>> getDashboardOverview() {
        
        log.info("관리자 대시보드 현황 조회 요청");
        
        RespDto<AdminDashboardOverviewRespDto> response = adminDashboardService.getDashboardOverview();
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 관리자 본사별 현황 조회
     * GET /api/v1/admin/dashboard/headquarters-stats
     */
    @GetMapping("/headquarters-stats")
    public ResponseEntity<RespDto<List<AdminHeadquartersStatsRespDto>>> getHeadquartersStats() {
        
        log.info("관리자 본사별 현황 조회 요청");
        
        RespDto<List<AdminHeadquartersStatsRespDto>> response = adminDashboardService.getHeadquartersStats();
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}