package com.maelcolium.telepesa.notification.mapper;

import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationDto.builder()
                .id(notification.getId())
                .notificationId(notification.getNotificationId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .status(notification.getStatus())
                .readAt(notification.getReadAt())
                .sentAt(notification.getSentAt())
                .deliveryMethod(notification.getDeliveryMethod())
                .recipientEmail(notification.getRecipientEmail())
                .recipientPhone(notification.getRecipientPhone())
                .deviceToken(notification.getDeviceToken())
                .templateId(notification.getTemplateId())
                .metadata(notification.getMetadata())
                .retryCount(notification.getRetryCount())
                .maxRetries(notification.getMaxRetries())
                .nextRetryAt(notification.getNextRetryAt())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }

    public Notification toEntity(NotificationDto notificationDto) {
        if (notificationDto == null) {
            return null;
        }

        return Notification.builder()
                .id(notificationDto.getId())
                .notificationId(notificationDto.getNotificationId())
                .userId(notificationDto.getUserId())
                .title(notificationDto.getTitle())
                .message(notificationDto.getMessage())
                .type(notificationDto.getType())
                .status(notificationDto.getStatus())
                .readAt(notificationDto.getReadAt())
                .sentAt(notificationDto.getSentAt())
                .deliveryMethod(notificationDto.getDeliveryMethod())
                .recipientEmail(notificationDto.getRecipientEmail())
                .recipientPhone(notificationDto.getRecipientPhone())
                .deviceToken(notificationDto.getDeviceToken())
                .templateId(notificationDto.getTemplateId())
                .metadata(notificationDto.getMetadata())
                .retryCount(notificationDto.getRetryCount())
                .maxRetries(notificationDto.getMaxRetries())
                .nextRetryAt(notificationDto.getNextRetryAt())
                .build();
    }
} 