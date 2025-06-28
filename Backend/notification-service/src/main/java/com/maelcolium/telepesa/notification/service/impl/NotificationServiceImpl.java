package com.maelcolium.telepesa.notification.service.impl;

import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.mapper.NotificationMapper;
import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.repository.NotificationRepository;
import com.maelcolium.telepesa.notification.service.NotificationService;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public NotificationDto createNotification(CreateNotificationRequest request) {
        log.info("Creating notification for user: {}, type: {}", request.getUserId(), request.getType());

        Notification notification = Notification.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .message(request.getMessage())
                .type(request.getType())
                .status(NotificationStatus.PENDING)
                .deliveryMethod(request.getDeliveryMethod())
                .recipientEmail(request.getRecipientEmail())
                .recipientPhone(request.getRecipientPhone())
                .deviceToken(request.getDeviceToken())
                .templateId(request.getTemplateId())
                .metadata(request.getMetadata())
                .retryCount(0)
                .maxRetries(3)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with ID: {}", savedNotification.getNotificationId());

        // Send notification asynchronously
        sendNotification(notificationMapper.toDto(savedNotification));

        return notificationMapper.toDto(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDto getNotificationByNotificationId(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with notification ID: " + notificationId));
        return notificationMapper.toDto(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotifications(Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findAll(pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByUserId(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByStatus(NotificationStatus status, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByStatus(status, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDto> getNotificationsByType(NotificationType type, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByType(type, pageable);
        return notifications.map(notificationMapper::toDto);
    }

    @Override
    public NotificationDto updateNotificationStatus(Long id, NotificationStatus status) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        notification.setStatus(status);
        if (status == NotificationStatus.SENT) {
            notification.setSentAt(LocalDateTime.now());
        } else if (status == NotificationStatus.READ) {
            notification.setReadAt(LocalDateTime.now());
        }

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification status updated to {} for notification ID: {}", status, updatedNotification.getNotificationId());

        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    public NotificationDto markAsRead(Long id) {
        return updateNotificationStatus(id, NotificationStatus.READ);
    }

    @Override
    public NotificationDto markAsReadByNotificationId(String notificationId) {
        Notification notification = notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with notification ID: " + notificationId));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        Notification updatedNotification = notificationRepository.save(notification);
        log.info("Notification marked as read: {}", notificationId);

        return notificationMapper.toDto(updatedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDto> getUnreadNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationRepository.findUnreadNotificationsByUserId(userId);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getReadNotificationCount(Long userId) {
        return notificationRepository.countReadNotificationsByUserId(userId);
    }

    @Override
    public void sendNotification(NotificationDto notificationDto) {
        log.info("Sending notification: {} via {}", notificationDto.getNotificationId(), notificationDto.getDeliveryMethod());

        try {
            // Simulate sending notification based on delivery method
            switch (notificationDto.getDeliveryMethod().toUpperCase()) {
                case "EMAIL":
                    sendEmailNotification(notificationDto);
                    break;
                case "SMS":
                    sendSmsNotification(notificationDto);
                    break;
                case "PUSH":
                    sendPushNotification(notificationDto);
                    break;
                case "IN_APP":
                    sendInAppNotification(notificationDto);
                    break;
                default:
                    log.warn("Unknown delivery method: {}", notificationDto.getDeliveryMethod());
            }

            // Update status to SENT
            updateNotificationStatus(notificationDto.getId(), NotificationStatus.SENT);

        } catch (Exception e) {
            log.error("Failed to send notification: {}", notificationDto.getNotificationId(), e);
            handleNotificationFailure(notificationDto.getId());
        }
    }

    @Override
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void processPendingNotifications() {
        log.info("Processing pending notifications...");
        List<Notification> pendingNotifications = notificationRepository.findPendingNotificationsForRetry(LocalDateTime.now());

        for (Notification notification : pendingNotifications) {
            try {
                sendNotification(notificationMapper.toDto(notification));
            } catch (Exception e) {
                log.error("Failed to process pending notification: {}", notification.getNotificationId(), e);
                handleNotificationFailure(notification.getId());
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void retryFailedNotifications() {
        log.info("Retrying failed notifications...");
        Page<Notification> failedNotifications = notificationRepository.findByStatus(NotificationStatus.FAILED, Pageable.unpaged());

        for (Notification notification : failedNotifications) {
            if (notification.getRetryCount() < notification.getMaxRetries()) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setStatus(NotificationStatus.PENDING);
                notification.setNextRetryAt(LocalDateTime.now().plusMinutes(5 * notification.getRetryCount()));
                notificationRepository.save(notification);
                log.info("Scheduled retry for notification: {} (attempt {})", notification.getNotificationId(), notification.getRetryCount());
            }
        }
    }

    private void sendEmailNotification(NotificationDto notification) {
        log.info("Sending email notification to: {}", notification.getRecipientEmail());
        // TODO: Implement actual email sending logic
        // For now, just simulate email sending
        try {
            Thread.sleep(100); // Simulate email sending delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendSmsNotification(NotificationDto notification) {
        log.info("Sending SMS notification to: {}", notification.getRecipientPhone());
        // TODO: Implement actual SMS sending logic
        // For now, just simulate SMS sending
        try {
            Thread.sleep(50); // Simulate SMS sending delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendPushNotification(NotificationDto notification) {
        log.info("Sending push notification to device: {}", notification.getDeviceToken());
        // TODO: Implement actual push notification logic
        // For now, just simulate push notification sending
        try {
            Thread.sleep(200); // Simulate push notification sending delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendInAppNotification(NotificationDto notification) {
        log.info("Sending in-app notification to user: {}", notification.getUserId());
        // TODO: Implement actual in-app notification logic
        // For now, just simulate in-app notification sending
        try {
            Thread.sleep(10); // Simulate in-app notification sending delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void handleNotificationFailure(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));

        notification.setRetryCount(notification.getRetryCount() + 1);
        
        if (notification.getRetryCount() >= notification.getMaxRetries()) {
            notification.setStatus(NotificationStatus.FAILED);
            log.error("Notification failed permanently after {} retries: {}", notification.getMaxRetries(), notification.getNotificationId());
        } else {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setNextRetryAt(LocalDateTime.now().plusMinutes(5 * notification.getRetryCount()));
            log.warn("Notification scheduled for retry {}: {}", notification.getRetryCount(), notification.getNotificationId());
        }

        notificationRepository.save(notification);
    }
} 