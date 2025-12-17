package com.inc.sh.repository;

import com.inc.sh.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    /**
     * 알림코드로 조회
     */
    Notification findByNotificationCode(Integer notificationCode);
    
    /**
     * 본사별 알림 목록 조회 (안읽음 우선, 생성일 내림차순)
     */
    @Query("SELECT n FROM Notification n WHERE n.hqCode = :hqCode " +
           "ORDER BY n.readYn ASC, n.createdAt DESC")
    List<Notification> findByHqCodeOrderByReadYnAscCreatedAtDesc(@Param("hqCode") Integer hqCode);
    
    /**
     * 본사별 안읽은 알림 개수 조회
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.hqCode = :hqCode AND n.readYn = 0")
    Integer countUnreadByHqCode(@Param("hqCode") Integer hqCode);
}