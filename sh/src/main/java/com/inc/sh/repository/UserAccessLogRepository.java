package com.inc.sh.repository;

import com.inc.sh.entity.UserAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAccessLogRepository extends JpaRepository<UserAccessLog, Integer> {
    
    /**
     * 사용자별 최근 접속 로그 조회 (성공만)
     */
    @Query(value = "SELECT * FROM user_access_log " +
           "WHERE user_type = :userType AND user_code = :userCode " +
           "AND login_status = 'SUCCESS' " +
           "ORDER BY access_time DESC LIMIT 10", nativeQuery = true)
    List<UserAccessLog> findRecentSuccessLogsByUser(
        @Param("userType") String userType, 
        @Param("userCode") String userCode
    );
    
    /**
     * 본사별 접속 통계 조회
     */
    @Query(value = "SELECT " +
           "DATE(access_time) as date, " +
           "COUNT(CASE WHEN login_status = 'SUCCESS' THEN 1 END) as success_count, " +
           "COUNT(CASE WHEN login_status = 'FAILURE' THEN 1 END) as failure_count " +
           "FROM user_access_log " +
           "WHERE hq_code = :hqCode " +
           "AND access_time >= :fromDate " +
           "GROUP BY DATE(access_time) " +
           "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> findAccessStatsByHqCode(
        @Param("hqCode") Integer hqCode, 
        @Param("fromDate") LocalDateTime fromDate
    );
    
    /**
     * IP별 실패 횟수 조회 (보안용)
     */
    @Query(value = "SELECT COUNT(*) FROM user_access_log " +
           "WHERE ip_address = :ipAddress " +
           "AND login_status = 'FAILURE' " +
           "AND access_time >= :fromTime", nativeQuery = true)
    Integer countFailuresByIp(
        @Param("ipAddress") String ipAddress,
        @Param("fromTime") LocalDateTime fromTime
    );
}