package com.maelcolium.telepesa.notification.dto;

import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {

    private Long id;
    private String notificationId;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime readAt;
    private LocalDateTime sentAt;
    private String deliveryMethod;
    private String recipientEmail;
    private String recipientPhone;
    private String deviceToken;
    private String templateId;
    private String metadata;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime nextRetryAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 