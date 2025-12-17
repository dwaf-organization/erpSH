package com.inc.sh.controller;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.notification.respDto.NotificationRespDto;
import com.inc.sh.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {
    
    private final NotificationService notificationService;
    
    /**
     * 알림 목록 조회
     * GET /api/v1/notification/list?hqCode={hqCode}
     */
    @GetMapping("/list")
    public ResponseEntity<RespDto<List<NotificationRespDto>>> getNotificationList(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("알림 목록 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<List<NotificationRespDto>> response = notificationService.getNotificationList(hqCode);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 알림 읽음 처리
     * PUT /api/v1/notification/read/{notificationCode}
     */
    @PutMapping("/read/{notificationCode}")
    public ResponseEntity<RespDto<String>> markAsRead(
            @PathVariable("notificationCode") Integer notificationCode) {
        
        log.info("알림 읽음 처리 요청 - notificationCode: {}", notificationCode);
        
        RespDto<String> response = notificationService.markAsRead(notificationCode);
        
        if (response.getCode() == 1) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 안읽은 알림 개수 조회
     * GET /api/v1/notification/unread-count?hqCode={hqCode}
     */
    @GetMapping("/unread-count")
    public ResponseEntity<RespDto<Integer>> getUnreadCount(
            @RequestParam("hqCode") Integer hqCode) {
        
        log.info("안읽은 알림 개수 조회 요청 - hqCode: {}", hqCode);
        
        RespDto<Integer> response = notificationService.getUnreadCount(hqCode);
        
        return ResponseEntity.ok(response);
    }
}