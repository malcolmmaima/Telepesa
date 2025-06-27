package com.maelcolium.telepesa.notification.repository;

import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByNotificationId_WithExistingNotification_ShouldReturnNotification() {
        // Given
        Notification notification = createTestNotification();
        entityManager.persistAndFlush(notification);

        // When
        Optional<Notification> result = notificationRepository.findByNotificationId("NOTIF-12345678");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getNotificationId()).isEqualTo("NOTIF-12345678");
    }

    @Test
    void findByNotificationId_WithNonExistingNotification_ShouldReturnEmpty() {
        // When
        Optional<Notification> result = notificationRepository.findByNotificationId("NON-EXISTENT");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnUserNotifications() {
        // Given
        Notification notification1 = createTestNotification();
        notification1.setUserId(10L);
        Notification notification2 = createTestNotification();
        notification2.setNotificationId("NOTIF-87654321");
        notification2.setUserId(10L);
        entityManager.persistAndFlush(notification1);
        entityManager.persistAndFlush(notification2);

        // When
        Page<Notification> result = notificationRepository.findByUserId(10L, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(n -> n.getUserId().equals(10L));
    }

    @Test
    void findByStatus_ShouldReturnStatusFilteredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setStatus(NotificationStatus.SENT);
        entityManager.persistAndFlush(notification);

        // When
        Page<Notification> result = notificationRepository.findByStatus(NotificationStatus.SENT, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void findByNotificationType_ShouldReturnTypeFilteredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setNotificationType(NotificationType.ACCOUNT_CREATED);
        entityManager.persistAndFlush(notification);

        // When
        Page<Notification> result = notificationRepository.findByNotificationType(NotificationType.ACCOUNT_CREATED, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNotificationType()).isEqualTo(NotificationType.ACCOUNT_CREATED);
    }

    @Test
    void findByDeliveryMethod_ShouldReturnMethodFilteredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setDeliveryMethod(DeliveryMethod.SMS);
        entityManager.persistAndFlush(notification);

        // When
        Page<Notification> result = notificationRepository.findByDeliveryMethod(DeliveryMethod.SMS, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDeliveryMethod()).isEqualTo(DeliveryMethod.SMS);
    }

    @Test
    void findByUserIdAndDateRange_ShouldReturnDateFilteredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setUserId(10L);
        notification.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(notification);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        Page<Notification> result = notificationRepository.findByUserIdAndDateRange(10L, startDate, endDate, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void findByUserIdAndStatus_ShouldReturnUserStatusFilteredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setUserId(10L);
        notification.setStatus(NotificationStatus.DELIVERED);
        entityManager.persistAndFlush(notification);

        // When
        Page<Notification> result = notificationRepository.findByUserIdAndStatus(10L, NotificationStatus.DELIVERED, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(10L);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(NotificationStatus.DELIVERED);
    }

    @Test
    void findByStatusAndScheduledAtBefore_ShouldReturnPendingNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setStatus(NotificationStatus.PENDING);
        notification.setScheduledAt(LocalDateTime.now().minusMinutes(5));
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByStatusAndScheduledAtBefore(NotificationStatus.PENDING, LocalDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void countByUserIdAndStatus_ShouldReturnCorrectCount() {
        // Given
        Notification notification1 = createTestNotification();
        notification1.setUserId(10L);
        notification1.setStatus(NotificationStatus.PENDING);
        Notification notification2 = createTestNotification();
        notification2.setNotificationId("NOTIF-87654321");
        notification2.setUserId(10L);
        notification2.setStatus(NotificationStatus.PENDING);
        entityManager.persistAndFlush(notification1);
        entityManager.persistAndFlush(notification2);

        // When
        long result = notificationRepository.countByUserIdAndStatus(10L, NotificationStatus.PENDING);

        // Then
        assertThat(result).isEqualTo(2);
    }

    @Test
    void findByRecipientEmail_ShouldReturnEmailNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setRecipientEmail("test@example.com");
        notification.setDeliveryMethod(DeliveryMethod.EMAIL);
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByRecipientEmail("test@example.com");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipientEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByRecipientPhone_ShouldReturnPhoneNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setRecipientPhone("+254700000000");
        notification.setDeliveryMethod(DeliveryMethod.SMS);
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByRecipientPhone("+254700000000");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecipientPhone()).isEqualTo("+254700000000");
    }

    @Test
    void findByRetryCountGreaterThanEqual_ShouldReturnRetryNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setRetryCount(3);
        notification.setMaxRetries(3);
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByRetryCountGreaterThanEqual(3);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRetryCount()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void findBySentAtBetween_ShouldReturnSentNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());
        entityManager.persistAndFlush(notification);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Notification> result = notificationRepository.findBySentAtBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSentAt()).isBetween(startDate, endDate);
    }

    @Test
    void findByDeliveredAtBetween_ShouldReturnDeliveredNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setStatus(NotificationStatus.DELIVERED);
        notification.setDeliveredAt(LocalDateTime.now());
        entityManager.persistAndFlush(notification);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Notification> result = notificationRepository.findByDeliveredAtBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeliveredAt()).isBetween(startDate, endDate);
    }

    @Test
    void findByReadAtBetween_ShouldReturnReadNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        entityManager.persistAndFlush(notification);

        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // When
        List<Notification> result = notificationRepository.findByReadAtBetween(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getReadAt()).isBetween(startDate, endDate);
    }

    @Test
    void findByMetadataKey_ShouldReturnNotificationsWithMetadata() {
        // Given
        Notification notification = createTestNotification();
        notification.setMetadata(Map.of("key1", "value1", "key2", "value2"));
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByMetadataKey("key1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetadata()).containsKey("key1");
    }

    @Test
    void findByTitleContainingIgnoreCase_ShouldReturnMatchingNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setTitle("Important Transaction Alert");
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByTitleContainingIgnoreCase("transaction");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).containsIgnoringCase("transaction");
    }

    @Test
    void findByMessageContainingIgnoreCase_ShouldReturnMatchingNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setMessage("Your transaction was successful");
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByMessageContainingIgnoreCase("successful");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).containsIgnoringCase("successful");
    }

    @Test
    void findByScheduledAtBefore_ShouldReturnOverdueNotifications() {
        // Given
        Notification notification = createTestNotification();
        notification.setScheduledAt(LocalDateTime.now().minusMinutes(10));
        entityManager.persistAndFlush(notification);

        // When
        List<Notification> result = notificationRepository.findByScheduledAtBefore(LocalDateTime.now());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScheduledAt()).isBefore(LocalDateTime.now());
    }

    private Notification createTestNotification() {
        return Notification.builder()
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .notificationType(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .recipientPhone("+254700000000")
                .metadata(Map.of("key", "value"))
                .scheduledAt(LocalDateTime.now().plusMinutes(5))
                .sentAt(null)
                .deliveredAt(null)
                .readAt(null)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }
} 