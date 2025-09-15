package com.maelcolium.telepesa.notification.controller;

import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.service.NotificationService;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a new notification", description = "Create and send a new notification")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());
        NotificationDto notification = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieve a notification by its ID")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> getNotification(@PathVariable Long id) {
        NotificationDto notification = notificationService.getNotification(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/notification-id/{notificationId}")
    @Operation(summary = "Get notification by notification ID", description = "Retrieve a notification by its notification ID")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> getNotificationByNotificationId(@PathVariable String notificationId) {
        NotificationDto notification = notificationService.getNotificationByNotificationId(notificationId);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieve all notifications with pagination")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get notifications by user ID", description = "Retrieve all notifications for a specific user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getNotificationsByUserId(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications for a user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getUnreadNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDto> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count")
    @Operation(summary = "Get notification counts", description = "Get read and unread notification counts for a user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationCounts> getNotificationCounts(@PathVariable Long userId) {
        long unreadCount = notificationService.getUnreadNotificationCount(userId);
        long readCount = notificationService.getReadNotificationCount(userId);

        NotificationCounts counts = NotificationCounts.builder()
                .userId(userId)
                .unreadCount(unreadCount)
                .readCount(readCount)
                .totalCount(unreadCount + readCount)
                .build();

        return ResponseEntity.ok(counts);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get notifications by status", description = "Retrieve all notifications with a specific status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByStatus(
            @PathVariable NotificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getNotificationsByStatus(status, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type", description = "Retrieve all notifications of a specific type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByType(
            @PathVariable NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getNotificationsByType(type, pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update notification status", description = "Update the status of a notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> updateNotificationStatus(
            @PathVariable Long id,
            @RequestParam NotificationStatus status) {
        NotificationDto notification = notificationService.updateNotificationStatus(id, status);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a notification as read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Long id) {
        NotificationDto notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/notification-id/{notificationId}/read")
    @Operation(summary = "Mark notification as read by notification ID", description = "Mark a notification as read using notification ID")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> markAsReadByNotificationId(@PathVariable String notificationId) {
        NotificationDto notification = notificationService.markAsReadByNotificationId(notificationId);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Send notification", description = "Manually trigger sending of a notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> sendNotification(@PathVariable Long id) {
        NotificationDto notification = notificationService.getNotification(id);
        notificationService.sendNotification(notification);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/process-pending")
    @Operation(summary = "Process pending notifications", description = "Manually trigger processing of pending notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> processPendingNotifications() {
        notificationService.processPendingNotifications();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retry-failed")
    @Operation(summary = "Retry failed notifications", description = "Manually trigger retry of failed notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> retryFailedNotifications() {
        notificationService.retryFailedNotifications();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delivery-method/{deliveryMethod}")
    @Operation(summary = "Get notifications by delivery method", description = "Retrieve notifications filtered by delivery method")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByDeliveryMethod(
            @PathVariable String deliveryMethod,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            DeliveryMethod method = DeliveryMethod.valueOf(deliveryMethod.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDto> notifications = notificationService.getNotificationsByDeliveryMethod(method, pageable);
            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid delivery method: " + deliveryMethod);
        }
    }

    @GetMapping("/user/{userId}/date-range")
    @Operation(summary = "Get notifications by date range", description = "Retrieve notifications for a user within a date range")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotificationsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            Pageable pageable = PageRequest.of(page, size);
            Page<NotificationDto> notifications = notificationService.getNotificationsByDateRange(userId, start, end, pageable);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending notifications", description = "Retrieve all pending notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationDto>> getPendingNotifications() {
        List<NotificationDto> notifications = notificationService.getPendingNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/failed")
    @Operation(summary = "Get failed notifications", description = "Retrieve all failed notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getFailedNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto> notifications = notificationService.getFailedNotifications(pageable);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/retry")
    @Operation(summary = "Retry failed notification", description = "Retry a specific failed notification")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationDto> retryFailedNotification(@PathVariable Long id) {
        NotificationDto notification = notificationService.retryFailedNotification(id);
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/user/{userId}/count-by-status")
    @Operation(summary = "Get notification count by status", description = "Get count of notifications for a user by status")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getNotificationCountByUserIdAndStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        try {
            NotificationStatus notificationStatus = NotificationStatus.valueOf(status.toUpperCase());
            Long count = notificationService.getNotificationCountByUserIdAndStatus(userId, notificationStatus);
            return ResponseEntity.ok(count);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Delete a notification by ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    // Helper class for notification counts response
    public static class NotificationCounts {
        private Long userId;
        private long unreadCount;
        private long readCount;
        private long totalCount;

        // Builder pattern
        public static NotificationCountsBuilder builder() {
            return new NotificationCountsBuilder();
        }

        public static class NotificationCountsBuilder {
            private Long userId;
            private long unreadCount;
            private long readCount;
            private long totalCount;

            public NotificationCountsBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }

            public NotificationCountsBuilder unreadCount(long unreadCount) {
                this.unreadCount = unreadCount;
                return this;
            }

            public NotificationCountsBuilder readCount(long readCount) {
                this.readCount = readCount;
                return this;
            }

            public NotificationCountsBuilder totalCount(long totalCount) {
                this.totalCount = totalCount;
                return this;
            }

            public NotificationCounts build() {
                NotificationCounts counts = new NotificationCounts();
                counts.userId = this.userId;
                counts.unreadCount = this.unreadCount;
                counts.readCount = this.readCount;
                counts.totalCount = this.totalCount;
                return counts;
            }
        }

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
        public long getReadCount() { return readCount; }
        public void setReadCount(long readCount) { this.readCount = readCount; }
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    }
} 