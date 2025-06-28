package com.maelcolium.telepesa.notification.repository;

import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void findByNotificationId_WithExistingNotification_ShouldReturnNotification() {
        // Given
        Notification notification = Notification.builder()
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .retryCount(0)
                .maxRetries(3)
                .build();
        entityManager.persistAndFlush(notification);

        // When
        Optional<Notification> found = notificationRepository.findByNotificationId("NOTIF-12345678");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Notification");
    }

    @Test
    void findByUserId_WithExistingNotifications_ShouldReturnUserNotifications() {
        // Given
        Notification notification = Notification.builder()
                .notificationId("NOTIF-USER-123")
                .userId(10L)
                .title("User Notification")
                .message("Test message")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .retryCount(0)
                .maxRetries(3)
                .build();
        entityManager.persistAndFlush(notification);

        // When
        Page<Notification> found = notificationRepository.findByUserId(10L, PageRequest.of(0, 10));

        // Then
        assertThat(found.getContent()).hasSize(1);
        assertThat(found.getContent().get(0).getUserId()).isEqualTo(10L);
    }
}
