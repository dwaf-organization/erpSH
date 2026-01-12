package com.inc.sh.controller.platform;

import com.inc.sh.service.platform.PlatformDashboardService;
import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.DashboardSalesReqDto;
import com.inc.sh.dto.platform.respDto.DashboardSalesRespDto;
import com.inc.sh.dto.platform.respDto.DashboardReviewRespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/dashboard")
@RequiredArgsConstructor
@Slf4j
public class PlatformDashboardController {
    
    private final PlatformDashboardService platformDashboardService;
    
    /**
     * 대시보드 매출 그래프 조회
     * GET /api/v1/platform/dashboard/sales-graph?hqCode=2&brandCode=2
     */
    @GetMapping("/sales-graph")
    public RespDto<DashboardSalesRespDto> getDashboardSales(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("brandCode") Integer brandCode) {
        
        try {
            log.info("대시보드 매출 그래프 조회 API 호출 - 본사: {}, 브랜드: {}", hqCode, brandCode);
            
            DashboardSalesReqDto reqDto = DashboardSalesReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .build();
            
            return platformDashboardService.getDashboardSales(reqDto);
            
        } catch (Exception e) {
            log.error("대시보드 매출 그래프 조회 API 오류", e);
            return RespDto.fail("대시보드 매출 그래프 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 대시보드 배달분석 조회 (5개월 플랫폼 비율)
     * GET /api/v1/platform/dashboard/delivery-analysis?hqCode=2&brandCode=2
     */
    @GetMapping("/delivery-analysis")
    public RespDto<DashboardSalesRespDto> getDeliveryAnalysis(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam("brandCode") Integer brandCode) {
        
        try {
            log.info("대시보드 배달분석 조회 API 호출 - 본사: {}, 브랜드: {}", hqCode, brandCode);
            
            DashboardSalesReqDto reqDto = DashboardSalesReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .build();
            
            // 같은 서비스 메서드 호출 (전체 데이터 반환)
            return platformDashboardService.getDashboardSales(reqDto);
            
        } catch (Exception e) {
            log.error("대시보드 배달분석 조회 API 오류", e);
            return RespDto.fail("대시보드 배달분석 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 대시보드 리뷰 종합 정보 조회 (5가지 한번에)
     * GET /api/v1/platform/dashboard/review-overview?hqCode=2&brandCode=0
     */
    @GetMapping("/review-overview")
    public RespDto<DashboardReviewRespDto> getDashboardReviews(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "brandCode", defaultValue = "0") Integer brandCode) {
        
        try {
            log.info("대시보드 리뷰 종합 정보 조회 API 호출 - 본사: {}, 브랜드: {}", hqCode, brandCode);
            
            DashboardSalesReqDto reqDto = DashboardSalesReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .build();
            
            return platformDashboardService.getDashboardReviews(reqDto);
            
        } catch (Exception e) {
            log.error("대시보드 리뷰 종합 정보 조회 API 오류", e);
            return RespDto.fail("대시보드 리뷰 종합 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}