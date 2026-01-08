package com.inc.sh.controller.platform;

import com.inc.sh.service.platform.PlatformOrderService;
import com.inc.sh.service.platform.PlatformSummaryService;
import com.inc.sh.common.dto.RespDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/platform/order")
@RequiredArgsConstructor
@Slf4j
public class PlatformOrderController {
    
    private final PlatformOrderService platformOrderService;
    private final PlatformSummaryService platformSummaryService;
    
    /**
     * 배민 주문내역 동기화 (테스트용)
     */
    @PostMapping("/sync/baemin")
    public RespDto<String> syncBaeminOrders(@RequestParam("hqCode") Integer hqCode) {
        try {
            log.info("배민 주문내역 동기화 API 호출 - 본사코드: {}", hqCode);
            
            // 플랫폼 동기화 실행
            RespDto<String> result = platformOrderService.syncBaeminOrders(hqCode);
            
            // 동기화 성공 시 집계 갱신
            if (result != null && result.getCode() == 1) {
                log.info("배민 동기화 성공 - 집계 갱신 시작");
                
                // 기본 브랜드 코드 1로 설정 (실제로는 세션에서 가져와야 함)
                Integer brandCode = 1;
                
                // 집계 테이블 갱신
                platformSummaryService.updateDailySalesSummary("배민", brandCode);
                platformSummaryService.updateStoreRanking("배민", brandCode);
                
                log.info("배민 집계 갱신 완료");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("배민 주문내역 동기화 API 오류", e);
            return RespDto.fail("배민 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 요기요 주문내역 동기화 (테스트용)
     */
    @PostMapping("/sync/yogiyo")
    public RespDto<String> syncYogiyoOrders(@RequestParam("hqCode") Integer hqCode) {
        try {
            log.info("요기요 주문내역 동기화 API 호출 - 본사코드: {}", hqCode);
            
            // 플랫폼 동기화 실행
            RespDto<String> result = platformOrderService.syncYogiyoOrders(hqCode);
            
            // 동기화 성공 시 집계 갱신
            if (result != null && result.getCode() == 1) {
                log.info("요기요 동기화 성공 - 집계 갱신 시작");
                
                // 기본 브랜드 코드 1로 설정 (실제로는 세션에서 가져와야 함)
                Integer brandCode = 1;
                
                // 집계 테이블 갱신
                platformSummaryService.updateDailySalesSummary("요기요", brandCode);
                platformSummaryService.updateStoreRanking("요기요", brandCode);
                
                log.info("요기요 집계 갱신 완료");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("요기요 주문내역 동기화 API 오류", e);
            return RespDto.fail("요기요 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 쿠팡이츠 주문내역 동기화 (테스트용)
     */
    @PostMapping("/sync/coupang")
    public RespDto<String> syncCoupangOrders(@RequestParam("hqCode") Integer hqCode) {
        try {
            log.info("쿠팡이츠 주문내역 동기화 API 호출 - 본사코드: {}", hqCode);
            
            // 플랫폼 동기화 실행
            RespDto<String> result = platformOrderService.syncCoupangOrders(hqCode);
            
            // 동기화 성공 시 집계 갱신
            if (result != null && result.getCode() == 1) {
                log.info("쿠팡이츠 동기화 성공 - 집계 갱신 시작");
                
                // 기본 브랜드 코드 1로 설정 (실제로는 세션에서 가져와야 함)
                Integer brandCode = 1;
                
                // 집계 테이블 갱신
                platformSummaryService.updateDailySalesSummary("쿠팡이츠", brandCode);
                platformSummaryService.updateStoreRanking("쿠팡이츠", brandCode);
                
                log.info("쿠팡이츠 집계 갱신 완료");
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("쿠팡이츠 주문내역 동기화 API 오류", e);
            return RespDto.fail("쿠팡이츠 주문내역 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 주문내역 동기화 상태 확인 (향후 구현)
     */
    @GetMapping("/sync/status")
    public RespDto<String> getSyncStatus() {
        // TODO: 구현 예정
        return RespDto.success("동기화 상태 확인 기능 준비중", "Not implemented yet");
    }
}