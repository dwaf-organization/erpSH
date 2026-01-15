package com.inc.sh.controller.scheduler;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.service.scheduler.PublicDataCollectionScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final PublicDataCollectionScheduler publicDataCollectionScheduler;

    /**
     * 수동으로 공공데이터 수집 스케줄러 실행 (테스트용)
     */
    @PostMapping("/collect/manual")
    public ResponseEntity<RespDto<String>> manualCollectPublicData() {
        log.info("수동 공공데이터 수집 스케줄러 실행 요청");
        
        try {
            // 별도 스레드에서 실행 (응답 시간 단축)
            new Thread(() -> {
                publicDataCollectionScheduler.executeDataCollectionManual();
            }).start();
            
            return ResponseEntity.ok(
                RespDto.success("공공데이터 수집 스케줄러가 시작되었습니다", "수집 진행 중...")
            );
            
        } catch (Exception e) {
            log.error("수동 스케줄러 실행 실패", e);
            return ResponseEntity.ok(
                RespDto.fail("스케줄러 실행 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    /**
     * 스케줄러 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<RespDto<String>> getSchedulerStatus() {
        return ResponseEntity.ok(
            RespDto.success("스케줄러 상태 조회 성공", "활성화됨 - 매시간 0분에 자동 실행")
        );
    }
}