package com.inc.sh.controller.platform;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.platform.reqDto.StoreReviewStatsReqDto;
import com.inc.sh.dto.platform.reqDto.PeriodReviewReqDto;
import com.inc.sh.dto.platform.respDto.StoreReviewStatsRespDto;
import com.inc.sh.dto.platform.respDto.PeriodReviewRespDto;
import com.inc.sh.service.platform.StoreReviewStatsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/review")
@RequiredArgsConstructor
@Slf4j
public class StoreReviewStatsController {
    
    private final StoreReviewStatsService storeReviewStatsService;
    
    /**
     * 매장별 리뷰현황 조회
     * 
     * GET /api/v1/platform/review/stats/stores?hqCode=2&brandCode=0&startYearMonth=202502&endYearMonth=202507
     * 
     * @param hqCode 거래처코드 (본사코드)
     * @param brandCode 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     * @param startYearMonth 시작년월 (YYYYMM)
     * @param endYearMonth 종료년월 (YYYYMM)
     * @return 매장별 리뷰 통계
     */
    @GetMapping("/stats/stores")
    public RespDto<StoreReviewStatsRespDto> getStoreReviewStats(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "brandCode", defaultValue = "0") Integer brandCode,
            @RequestParam("startYearMonth") String startYearMonth,
            @RequestParam("endYearMonth") String endYearMonth) {
        
        try {
            log.info("매장별 리뷰현황 조회 API 호출 - 거래처: {}, 브랜드: {}, 기간: {}~{}", 
                    hqCode, brandCode, startYearMonth, endYearMonth);
            
            StoreReviewStatsReqDto reqDto = StoreReviewStatsReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .startYearMonth(startYearMonth)
                    .endYearMonth(endYearMonth)
                    .build();
            
            return storeReviewStatsService.getStoreReviewStats(reqDto);
            
        } catch (Exception e) {
            log.error("매장별 리뷰현황 조회 API 오류", e);
            return RespDto.fail("매장별 리뷰현황 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 기간별 리뷰 상세 조회
     * 
     * GET /api/v1/platform/review/details?hqCode=2&brandCode=0&appName=배민&startYm=202501&endYm=202512
     * 
     * @param hqCode 거래처코드 (본사코드)
     * @param brandCode 브랜드코드 (0: 전체, 특정값: 해당 브랜드만)
     * @param appName 앱 이름 (배민/요기요/쿠팡이츠/전체)
     * @param startYm 시작년월 (YYYYMM)
     * @param endYm 종료년월 (YYYYMM)
     * @return 기간별 리뷰 상세 목록
     */
    @GetMapping("/details")
    public RespDto<PeriodReviewRespDto> getPeriodReviews(
            @RequestParam("hqCode") Integer hqCode,
            @RequestParam(value = "brandCode", defaultValue = "0") Integer brandCode,
            @RequestParam(value = "appName", defaultValue = "전체") String appName,
            @RequestParam("startYm") String startYm,
            @RequestParam("endYm") String endYm) {
        
        try {
            log.info("기간별 리뷰조회 API 호출 - 거래처: {}, 브랜드: {}, 앱: {}, 기간: {}~{}", 
                    hqCode, brandCode, appName, startYm, endYm);
            
            PeriodReviewReqDto reqDto = PeriodReviewReqDto.builder()
                    .hqCode(hqCode)
                    .brandCode(brandCode)
                    .appName(appName)
                    .startYm(startYm)
                    .endYm(endYm)
                    .build();
            
            return storeReviewStatsService.getPeriodReviews(reqDto);
            
        } catch (Exception e) {
            log.error("기간별 리뷰조회 API 오류", e);
            return RespDto.fail("기간별 리뷰조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}