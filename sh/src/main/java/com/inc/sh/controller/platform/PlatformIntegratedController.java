package com.inc.sh.controller.platform;

import com.inc.sh.service.platform.PlatformOrderService;
import com.inc.sh.service.platform.PlatformSummaryService;
import com.inc.sh.common.dto.RespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/platform/order")
@RequiredArgsConstructor
@Slf4j
public class PlatformIntegratedController {
    
    private final PlatformOrderService platformOrderService;
    private final PlatformSummaryService platformSummaryService;
    
    /**
     * 통합 플랫폼 동기화 (순차 실행)
     * - 즉시: 배민 조회 + DB저장 + 집계갱신
     * - 2분후: 요기요 조회 + DB저장 + 집계갱신  
     * - 3분후: 쿠팡이츠 조회 + DB저장 + 집계갱신
     */
    @PostMapping("/sync/all")
    public RespDto<String> syncAllPlatforms(@RequestParam("hqCode") Integer hqCode) {
        try {
            log.info("=== 통합 플랫폼 동기화 시작 - 본사코드: {} ===", hqCode);
            
            // 즉시 배민 실행
            log.info("1. 배민 동기화 시작 - 본사: {}", hqCode);
            executeAndUpdateSummary("배민", hqCode);
            
            // 2분 후 요기요 실행
            CompletableFuture.delayedExecutor(2, TimeUnit.MINUTES).execute(() -> {
                log.info("2. 요기요 동기화 시작 (2분 후) - 본사: {}", hqCode);
                executeAndUpdateSummary("요기요", hqCode);
            });
            
            // 3분 후 쿠팡이츠 실행
            CompletableFuture.delayedExecutor(3, TimeUnit.MINUTES).execute(() -> {
                log.info("3. 쿠팡이츠 동기화 시작 (3분 후) - 본사: {}", hqCode);
                executeAndUpdateSummary("쿠팡이츠", hqCode);
            });
            
            String message = "통합 동기화 시작 (본사: " + hqCode + ") - 배민(즉시), 요기요(2분후), 쿠팡이츠(3분후)";
            log.info(message);
            
            return RespDto.success(message, message);
            
        } catch (Exception e) {
            log.error("통합 플랫폼 동기화 중 오류 발생 - 본사: {}", hqCode, e);
            return RespDto.fail("통합 플랫폼 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 플랫폼별 실행 및 집계 갱신
     */
    @Async
    private void executeAndUpdateSummary(String platform, Integer hqCode) {
        try {
            RespDto<String> result = null;
            
            // 플랫폼별 동기화 실행
            switch (platform) {
                case "배민":
                    result = platformOrderService.syncBaeminOrders(hqCode);
                    break;
                case "요기요":
                    result = platformOrderService.syncYogiyoOrders(hqCode);
                    break;
                case "쿠팡이츠":
                    result = platformOrderService.syncCoupangOrders(hqCode);
                    break;
                default:
                    log.error("지원하지 않는 플랫폼: {}", platform);
                    return;
            }
            
            // 동기화 성공 시 집계 갱신
            if (result != null && result.getCode() == 1) {
                log.info("{} 동기화 성공 (본사: {}) - 집계 갱신 시작", platform, hqCode);
                
                // 기본 브랜드 코드 1로 설정 (실제로는 세션에서 가져와야 함)
                Integer brandCode = 1;
                
                // 집계 테이블 갱신
                platformSummaryService.updateDailySalesSummary(platform, brandCode);
                platformSummaryService.updateStoreRanking(platform, brandCode);
                
                log.info("{} 집계 갱신 완료 (본사: {})", platform, hqCode);
            } else {
                log.warn("{} 동기화 실패 (본사: {}) - 집계 갱신 스킵", platform, hqCode);
            }
            
        } catch (Exception e) {
            log.error("{} 동기화 및 집계 갱신 중 오류 발생 (본사: {})", platform, hqCode, e);
        }
    }
    
    /**
     * 개별 플랫폼 동기화 + 집계 갱신 (테스트용)
     */
    @PostMapping("/sync/{platform}")
    public RespDto<String> syncPlatformWithSummary(@PathVariable String platform, @RequestParam("hqCode") Integer hqCode) {
        try {
            log.info("개별 {} 동기화 + 집계 갱신 시작 - 본사: {}", platform, hqCode);
            
            executeAndUpdateSummary(platform, hqCode);
            
            String message = platform + " 동기화 및 집계 갱신 시작 (본사: " + hqCode + ")";
            return RespDto.success(message, message);
            
        } catch (Exception e) {
            log.error("{} 동기화 및 집계 갱신 중 오류 발생 - 본사: {}", platform, hqCode, e);
            return RespDto.fail(platform + " 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 집계 테이블 강제 갱신 (데이터 수동 갱신용)
     */
    @PostMapping("/summary/update")
    public RespDto<String> updateSummaryTables(@RequestParam(required = false) Integer brandCode) {
        try {
            log.info("집계 테이블 강제 갱신 시작 - 브랜드: {}", brandCode);
            
            if (brandCode == null) {
                brandCode = 1; // 기본값
            }
            
            // 모든 플랫폼 집계 갱신
            String[] platforms = {"배민", "요기요", "쿠팡이츠"};
            
            for (String platform : platforms) {
                platformSummaryService.updateDailySalesSummary(platform, brandCode);
                platformSummaryService.updateStoreRanking(platform, brandCode);
            }
            
            String message = "모든 플랫폼 집계 테이블 갱신 완료 - 브랜드: " + brandCode;
            log.info(message);
            
            return RespDto.success(message, message);
            
        } catch (Exception e) {
            log.error("집계 테이블 갱신 중 오류 발생", e);
            return RespDto.fail("집계 테이블 갱신 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}