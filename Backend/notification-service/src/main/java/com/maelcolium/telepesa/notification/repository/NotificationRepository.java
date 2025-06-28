package com.maelcolium.telepesa.notification.repository;

import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByNotificationId(String notificationId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    Page<Notification> findByType(NotificationType type, Pageable pageable);

    Page<Notification> findByUserIdAndStatus(Long userId, NotificationStatus status, Pageable pageable);

    Page<Notification> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    List<Notification> findByStatusAndNextRetryAtBefore(NotificationStatus status, LocalDateTime before);

    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetries);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readAt IS NULL")
    long countUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.readAt IS NOT NULL")
    long countReadNotificationsByUserId(@Param("userId") Long userId);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)")
    List<Notification> findPendingNotificationsForRetry(@Param("status") NotificationStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.status = :status AND n.createdAt >= :since")
    Page<Notification> findRecentNotificationsByUserAndStatus(
            @Param("userId") Long userId,
            @Param("status") NotificationStatus status,
            @Param("since") LocalDateTime since,
            Pageable pageable
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") NotificationStatus status);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.type = :type")
    long countByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetries AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)")
    List<Notification> findNotificationsForRetry(@Param("status") NotificationStatus status, @Param("now") LocalDateTime now);
} 