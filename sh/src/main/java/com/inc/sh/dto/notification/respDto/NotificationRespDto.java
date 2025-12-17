package com.inc.sh.dto.notification.respDto;

import com.inc.sh.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRespDto {
    
    private Integer notificationCode;
    private Integer hqCode;
    private Integer customerCode;
    private String customerName;
    private String referenceName;
    private String referenceCode;
    private Integer readYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Entity → RespDto 변환
     */
    public static NotificationRespDto from(Notification notification) {
        return NotificationRespDto.builder()
                .notificationCode(notification.getNotificationCode())
                .hqCode(notification.getHqCode())
                .customerCode(notification.getCustomerCode())
                .customerName(notification.getCustomerName())
                .referenceName(notification.getReferenceName())
                .referenceCode(notification.getReferenceCode())
                .readYn(notification.getReadYn())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}