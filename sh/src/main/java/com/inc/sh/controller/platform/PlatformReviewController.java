package com.inc.sh.controller.platform;

import com.inc.sh.service.platform.PlatformReviewService;
import com.inc.sh.common.dto.RespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/platform/review")
@RequiredArgsConstructor
@Slf4j
public class PlatformReviewController {
    
    private final PlatformReviewService platformReviewService;
    
    /**
     * 배민 리뷰 동기화
     * POST /api/v1/platform/review/sync/baemin?hqCode=2
     */
    @PostMapping("/sync/baemin")
    public RespDto<Map<String, Object>> syncBaeminReviews(
            @RequestParam("hqCode") Integer hqCode) {
        
        try {
            log.info("배민 리뷰 동기화 API 호출 - 본사: {}", hqCode);
            
            return platformReviewService.syncBaeminReviews(hqCode);
            
        } catch (Exception e) {
            log.error("배민 리뷰 동기화 API 오류", e);
            return RespDto.fail("배민 리뷰 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 요기요 리뷰 동기화
     * POST /api/v1/platform/review/sync/yogiyo?hqCode=2
     */
    @PostMapping("/sync/yogiyo")
    public RespDto<Map<String, Object>> syncYogiyoReviews(
            @RequestParam("hqCode") Integer hqCode) {
        
        try {
            log.info("요기요 리뷰 동기화 API 호출 - 본사: {}", hqCode);
            
            return platformReviewService.syncYogiyoReviews(hqCode);
            
        } catch (Exception e) {
            log.error("요기요 리뷰 동기화 API 오류", e);
            return RespDto.fail("요기요 리뷰 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 쿠팡이츠 리뷰 동기화
     * POST /api/v1/platform/review/sync/coupang?hqCode=2
     */
    @PostMapping("/sync/coupang")
    public RespDto<Map<String, Object>> syncCoupangReviews(
            @RequestParam("hqCode") Integer hqCode) {
        
        try {
            log.info("쿠팡이츠 리뷰 동기화 API 호출 - 본사: {}", hqCode);
            
            return platformReviewService.syncCoupangReviews(hqCode);
            
        } catch (Exception e) {
            log.error("쿠팡이츠 리뷰 동기화 API 오류", e);
            return RespDto.fail("쿠팡이츠 리뷰 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 전체 플랫폼 리뷰 동기화
     * POST /api/v1/platform/review/sync/all?hqCode=2
     */
    @PostMapping("/sync/all")
    public RespDto<Map<String, Object>> syncAllReviews(
            @RequestParam("hqCode") Integer hqCode) {
        
        try {
            log.info("전체 플랫폼 리뷰 동기화 API 호출 - 본사: {}", hqCode);
            
            return platformReviewService.syncAllReviews(hqCode);
            
        } catch (Exception e) {
            log.error("전체 플랫폼 리뷰 동기화 API 오류", e);
            return RespDto.fail("전체 플랫폼 리뷰 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}