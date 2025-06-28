package com.maelcolium.telepesa.notification.repository;

import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private Notification testNotification1;
    private Notification testNotification2;
    private Notification testNotification3;

    @BeforeEach
    void setUp() {
        // Create test data
        testNotification1 = Notification.builder()
                .notificationId("NOTIF-001")
                .userId(10L)
                .title("Test Notification 1")
                .message("Test message 1")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test1@example.com")
                .retryCount(0)
                .maxRetries(3)
                .readAt(null)
                .nextRetryAt(LocalDateTime.now().plusMinutes(5))
                .build();

        testNotification2 = Notification.builder()
                .notificationId("NOTIF-002")
                .userId(10L)
                .title("Test Notification 2")
                .message("Test message 2")
                .type(NotificationType.ACCOUNT_CREATED)
                .status(NotificationStatus.SENT)
                .deliveryMethod(DeliveryMethod.SMS)
                .recipientPhone("+1234567890")
                .retryCount(1)
                .maxRetries(3)
                .readAt(LocalDateTime.now())
                .build();

        testNotification3 = Notification.builder()
                .notificationId("NOTIF-003")
                .userId(20L)
                .title("Test Notification 3")
                .message("Test message 3")
                .type(NotificationType.TRANSACTION_FAILED)
                .status(NotificationStatus.FAILED)
                .deliveryMethod(DeliveryMethod.PUSH_NOTIFICATION)
                .deviceToken("device123")
                .retryCount(2)
                .maxRetries(3)
                .nextRetryAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    @Test
    void findByNotificationId_WithExistingNotification_ShouldReturnNotification() {
        // Given
        entityManager.persistAndFlush(testNotification1);

        // When
        Optional<Notification> found = notificationRepository.findByNotificationId("NOTIF-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Notification 1");
    }

    @Test
    void findByUserId_WithExistingNotifications_ShouldReturnUserNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        Page<Notification> found = notificationRepository.findByUserId(10L, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(2);
        assertThat(found.getContent()).allMatch(n -> n.getUserId().equals(10L));
    }

    @Test
    void findByStatus_WithExistingStatus_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        Page<Notification> found = notificationRepository.findByStatus(NotificationStatus.PENDING, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getStatus()).isEqualTo(NotificationStatus.PENDING);
    }

    @Test
    void findByType_WithExistingType_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        Page<Notification> found = notificationRepository.findByType(NotificationType.TRANSACTION_SUCCESS, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getType()).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
    }

    @Test
    void findByUserIdAndStatus_WithExistingUserAndStatus_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        Page<Notification> found = notificationRepository.findByUserIdAndStatus(10L, NotificationStatus.SENT, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getUserId()).isEqualTo(10L);
        assertThat(found.getContent().get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void findByUserIdAndType_WithExistingUserAndType_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        Page<Notification> found = notificationRepository.findByUserIdAndType(10L, NotificationType.ACCOUNT_CREATED, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getUserId()).isEqualTo(10L);
        assertThat(found.getContent().get(0).getType()).isEqualTo(NotificationType.ACCOUNT_CREATED);
    }

    @Test
    void findByStatusAndNextRetryAtBefore_WithRetryableNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification3);

        // When
        List<Notification> found = notificationRepository.findByStatusAndNextRetryAtBefore(
                NotificationStatus.FAILED, LocalDateTime.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(found.get(0).getNextRetryAt()).isBefore(LocalDateTime.now());
    }

    @Test
    void findByStatusAndRetryCountLessThan_WithRetryableNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1);
        entityManager.persistAndFlush(testNotification2);
        entityManager.persistAndFlush(testNotification3);

        // When
        List<Notification> found = notificationRepository.findByStatusAndRetryCountLessThan(
                NotificationStatus.FAILED, 3);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(found.get(0).getRetryCount()).isLessThan(3);
    }

    @Test
    void findUnreadNotificationsByUserId_WithUnreadNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification1); // unread (readAt is null)
        entityManager.persistAndFlush(testNotification2); // read (readAt is set)

        // When
        List<Notification> found = notificationRepository.findUnreadNotificationsByUserId(10L);

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getReadAt()).isNull();
        assertThat(found.get(0).getUserId()).isEqualTo(10L);
    }

    @Test
    void countUnreadNotificationsByUserId_WithUnreadNotifications_ShouldReturnCount() {
        // Given
        entityManager.persistAndFlush(testNotification1); // unread
        entityManager.persistAndFlush(testNotification2); // read

        // When
        long count = notificationRepository.countUnreadNotificationsByUserId(10L);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countReadNotificationsByUserId_WithReadNotifications_ShouldReturnCount() {
        // Given
        entityManager.persistAndFlush(testNotification1); // unread
        entityManager.persistAndFlush(testNotification2); // read

        // When
        long count = notificationRepository.countReadNotificationsByUserId(10L);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findPendingNotificationsForRetry_WithRetryableNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification3); // failed with past retry time

        // When
        List<Notification> found = notificationRepository.findPendingNotificationsForRetry(
                NotificationStatus.FAILED, LocalDateTime.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(found.get(0).getRetryCount()).isLessThan(found.get(0).getMaxRetries());
    }

    @Test
    void findRecentNotificationsByUserAndStatus_WithRecentNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification2); // sent notification

        // When
        Page<Notification> found = notificationRepository.findRecentNotificationsByUserAndStatus(
                10L, NotificationStatus.SENT, LocalDateTime.now().minusHours(1), PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getUserId()).isEqualTo(10L);
        assertThat(found.getContent().get(0).getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void countByUserIdAndStatus_WithUserNotifications_ShouldReturnCount() {
        // Given
        entityManager.persistAndFlush(testNotification1); // pending
        entityManager.persistAndFlush(testNotification2); // sent

        // When
        long count = notificationRepository.countByUserIdAndStatus(10L, NotificationStatus.SENT);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void countByUserIdAndType_WithUserNotifications_ShouldReturnCount() {
        // Given
        entityManager.persistAndFlush(testNotification1); // transaction success
        entityManager.persistAndFlush(testNotification2); // account created

        // When
        long count = notificationRepository.countByUserIdAndType(10L, NotificationType.TRANSACTION_SUCCESS);

        // Then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findNotificationsForRetry_WithRetryableNotifications_ShouldReturnNotifications() {
        // Given
        entityManager.persistAndFlush(testNotification3); // failed with past retry time

        // When
        List<Notification> found = notificationRepository.findNotificationsForRetry(
                NotificationStatus.FAILED, LocalDateTime.now());

        // Then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(found.get(0).getRetryCount()).isLessThan(found.get(0).getMaxRetries());
    }

    @Test
    void findByNotificationId_WithNonExistingNotification_ShouldReturnEmpty() {
        // When
        Optional<Notification> found = notificationRepository.findByNotificationId("NON-EXISTING");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_WithNonExistingUser_ShouldReturnEmptyPage() {
        // When
        Page<Notification> found = notificationRepository.findByUserId(999L, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).isEmpty();
    }

    @Test
    void countUnreadNotificationsByUserId_WithNoNotifications_ShouldReturnZero() {
        // When
        long count = notificationRepository.countUnreadNotificationsByUserId(999L);

        // Then
        assertThat(count).isEqualTo(0);
    }
}
