package com.inc.sh.service;

import com.inc.sh.common.dto.RespDto;
import com.inc.sh.dto.notification.respDto.NotificationRespDto;
import com.inc.sh.entity.Notification;
import com.inc.sh.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    
    /**
     * 알림 생성 (주문 생성시 호출)
     */
    @Transactional
    public void createOrderNotification(Integer hqCode, Integer customerCode, String customerName, String orderNo) {
        try {
            Notification notification = Notification.builder()
                    .hqCode(hqCode)
                    .customerCode(customerCode)
                    .customerName(customerName)
                    .referenceName("주문")
                    .referenceCode(orderNo)
                    .readYn(0) // 기본값 안읽음
                    .build();
            
            notificationRepository.save(notification);
            
            log.info("주문 알림 생성 완료 - hqCode: {}, customerName: {}, orderNo: {}", 
                    hqCode, customerName, orderNo);
            
        } catch (Exception e) {
            log.error("주문 알림 생성 실패 - orderNo: {}", orderNo, e);
            // 알림 생성 실패는 주문 로직에 영향주지 않음
        }
    }
    
    /**
     * 본사별 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public RespDto<List<NotificationRespDto>> getNotificationList(Integer hqCode) {
        try {
            log.info("알림 목록 조회 시작 - hqCode: {}", hqCode);
            
            List<Notification> notifications = notificationRepository.findByHqCodeOrderByReadYnAscCreatedAtDesc(hqCode);
            
            List<NotificationRespDto> responseList = notifications.stream()
                    .map(NotificationRespDto::from)
                    .collect(Collectors.toList());
            
            log.info("알림 목록 조회 완료 - hqCode: {}, 총 {}건", hqCode, responseList.size());
            
            return RespDto.success("알림 목록 조회 성공", responseList);
            
        } catch (Exception e) {
            log.error("알림 목록 조회 실패 - hqCode: {}", hqCode, e);
            return RespDto.fail("알림 목록 조회 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 알림 읽음 처리
     */
    @Transactional
    public RespDto<String> markAsRead(Integer notificationCode) {
        try {
            log.info("알림 읽음 처리 시작 - notificationCode: {}", notificationCode);
            
            Notification notification = notificationRepository.findByNotificationCode(notificationCode);
            if (notification == null) {
                return RespDto.fail("존재하지 않는 알림입니다");
            }
            
            // 이미 읽음 처리된 경우
            if (notification.getReadYn() == 1) {
                return RespDto.success("이미 읽음 처리된 알림입니다", "처리완료");
            }
            
            // 읽음 처리
            notification.setReadYn(1);
            notificationRepository.save(notification);
            
            log.info("알림 읽음 처리 완료 - notificationCode: {}", notificationCode);
            
            return RespDto.success("알림 읽음 처리 완료", "처리완료");
            
        } catch (Exception e) {
            log.error("알림 읽음 처리 실패 - notificationCode: {}", notificationCode, e);
            return RespDto.fail("알림 읽음 처리 중 오류가 발생했습니다");
        }
    }
    
    /**
     * 본사별 안읽은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public RespDto<Integer> getUnreadCount(Integer hqCode) {
        try {
            Integer unreadCount = notificationRepository.countUnreadByHqCode(hqCode);
            
            log.info("안읽은 알림 개수 조회 - hqCode: {}, unreadCount: {}", hqCode, unreadCount);
            
            return RespDto.success("안읽은 알림 개수 조회 성공", unreadCount);
            
        } catch (Exception e) {
            log.error("안읽은 알림 개수 조회 실패 - hqCode: {}", hqCode, e);
            return RespDto.fail("안읽은 알림 개수 조회 중 오류가 발생했습니다");
        }
    }
}