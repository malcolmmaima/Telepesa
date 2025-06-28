package com.maelcolium.telepesa.notification.service;

import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationService {

    NotificationDto createNotification(CreateNotificationRequest request);

    NotificationDto getNotification(Long id);

    NotificationDto getNotificationByNotificationId(String notificationId);

    Page<NotificationDto> getNotifications(Pageable pageable);

    Page<NotificationDto> getNotificationsByUserId(Long userId, Pageable pageable);

    Page<NotificationDto> getNotificationsByStatus(NotificationStatus status, Pageable pageable);

    Page<NotificationDto> getNotificationsByType(NotificationType type, Pageable pageable);

    Page<NotificationDto> getNotificationsByDeliveryMethod(DeliveryMethod deliveryMethod, Pageable pageable);

    Page<NotificationDto> getNotificationsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable);

    List<NotificationDto> getPendingNotifications();

    Page<NotificationDto> getFailedNotifications(Pageable pageable);

    NotificationDto updateNotificationStatus(Long id, NotificationStatus status);

    NotificationDto markAsRead(Long id);

    NotificationDto markAsReadByNotificationId(String notificationId);

    List<NotificationDto> getUnreadNotificationsByUserId(Long userId);

    long getUnreadNotificationCount(Long userId);

    long getReadNotificationCount(Long userId);

    long getNotificationCountByUserIdAndStatus(Long userId, NotificationStatus status);

    NotificationDto retryFailedNotification(Long id);

    void deleteNotification(Long id);

    void sendNotification(NotificationDto notification);

    void processPendingNotifications();

    void retryFailedNotifications();
} 