package com.maelcolium.telepesa.notification.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    void testNotificationBuilder_WithAllFields_ShouldCreateValidNotification() {
        // Given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "mobile");
        metadata.put("campaign", "welcome");

        // When
        Notification notification = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Welcome")
                .message("Welcome to Telepesa!")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .metadata(metadata)
                .retryCount(0)
                .maxRetries(3)
                .build();

        // Then
        assertThat(notification.getNotificationId()).isEqualTo("NOTIF_001");
        assertThat(notification.getUserId()).isEqualTo(1L);
        assertThat(notification.getTitle()).isEqualTo("Welcome");
        assertThat(notification.getMessage()).isEqualTo("Welcome to Telepesa!");
        assertThat(notification.getType()).isEqualTo(NotificationType.WELCOME_MESSAGE);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getDeliveryMethod()).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(notification.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(notification.getMetadata()).containsEntry("source", "mobile");
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getMaxRetries()).isEqualTo(3);
    }

    @Test
    void testNotificationEqualsAndHashCode_WithSameData_ShouldBeEqual() {
        // Given
        Notification notification1 = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.ACCOUNT_CREATED)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        Notification notification2 = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.ACCOUNT_CREATED)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // When & Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    void testNotificationToString_ShouldContainKeyFields() {
        // Given
        Notification notification = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.ACCOUNT_CREATED)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // When
        String result = notification.toString();

        // Then
        assertThat(result).contains("NOTIF_001");
        assertThat(result).contains("Test");
        assertThat(result).contains("ACCOUNT_CREATED");
        assertThat(result).contains("EMAIL");
    }

    @Test
    void testNotificationGettersAndSetters_ShouldWorkCorrectly() {
        // Given
        Notification notification = new Notification();
        LocalDateTime now = LocalDateTime.now();

        // When
        notification.setNotificationId("NOTIF_002");
        notification.setUserId(2L);
        notification.setTitle("Updated Title");
        notification.setMessage("Updated message");
        notification.setType(NotificationType.TRANSACTION_SUCCESS);
        notification.setStatus(NotificationStatus.SENT);
        notification.setDeliveryMethod(DeliveryMethod.SMS);
        notification.setRecipientPhone("+254700000000");
        notification.setSentAt(now);
        notification.setRetryCount(1);

        // Then
        assertThat(notification.getNotificationId()).isEqualTo("NOTIF_002");
        assertThat(notification.getUserId()).isEqualTo(2L);
        assertThat(notification.getTitle()).isEqualTo("Updated Title");
        assertThat(notification.getMessage()).isEqualTo("Updated message");
        assertThat(notification.getType()).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getDeliveryMethod()).isEqualTo(DeliveryMethod.SMS);
        assertThat(notification.getRecipientPhone()).isEqualTo("+254700000000");
        assertThat(notification.getSentAt()).isEqualTo(now);
        assertThat(notification.getRetryCount()).isEqualTo(1);
    }
}
