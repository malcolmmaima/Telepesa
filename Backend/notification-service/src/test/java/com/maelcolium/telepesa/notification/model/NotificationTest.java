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

    @Test
    void builder_ShouldCreateNotificationWithMetadata() {
        // Given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // When
        Notification notification = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .metadata(metadata)
                .retryCount(0)
                .maxRetries(3)
                .build();

        // Then
        assertThat(notification).isNotNull();
        assertThat(notification.getNotificationId()).isEqualTo("NOTIF_001");
        assertThat(notification.getMetadata()).hasSize(2);
        assertThat(notification.getMetadata().get("key1")).isEqualTo("value1");
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Given
        Notification notification1 = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .build();

        Notification notification2 = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .build();

        // Then
        assertThat(notification1).isEqualTo(notification2);
        assertThat(notification1.hashCode()).isEqualTo(notification2.hashCode());
    }

    @Test
    void toString_ShouldReturnStringRepresentation() {
        // Given
        Notification notification = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .build();

        // When
        String result = notification.toString();

        // Then
        assertThat(result).contains("NOTIF_001");
        assertThat(result).contains("Test");
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
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
        notification.setReadAt(now);
        notification.setSentAt(now);
        notification.setDeliveryMethod(DeliveryMethod.SMS);
        notification.setRecipientEmail("test@example.com");
        notification.setRecipientPhone("+1234567890");
        notification.setDeviceToken("device123");
        notification.setTemplateId("template123");
        notification.setRetryCount(1);
        notification.setMaxRetries(5);
        notification.setNextRetryAt(now);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");
        notification.setMetadata(metadata);

        // Then
        assertThat(notification.getNotificationId()).isEqualTo("NOTIF_002");
        assertThat(notification.getUserId()).isEqualTo(2L);
        assertThat(notification.getTitle()).isEqualTo("Updated Title");
        assertThat(notification.getMessage()).isEqualTo("Updated message");
        assertThat(notification.getType()).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getReadAt()).isEqualTo(now);
        assertThat(notification.getSentAt()).isEqualTo(now);
        assertThat(notification.getDeliveryMethod()).isEqualTo(DeliveryMethod.SMS);
        assertThat(notification.getRecipientEmail()).isEqualTo("test@example.com");
        assertThat(notification.getRecipientPhone()).isEqualTo("+1234567890");
        assertThat(notification.getDeviceToken()).isEqualTo("device123");
        assertThat(notification.getTemplateId()).isEqualTo("template123");
        assertThat(notification.getRetryCount()).isEqualTo(1);
        assertThat(notification.getMaxRetries()).isEqualTo(5);
        assertThat(notification.getNextRetryAt()).isEqualTo(now);
        assertThat(notification.getMetadata()).containsEntry("testKey", "testValue");
    }

    @Test
    void onCreate_ShouldSetDefaultValues() {
        // Given
        Notification notification = new Notification();
        
        // When
        notification.onCreate();
        
        // Then
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(notification.getRetryCount()).isEqualTo(0);
        assertThat(notification.getMaxRetries()).isEqualTo(3);
    }

    @Test
    void builderPattern_WithAllFields_ShouldCreateCompleteNotification() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", "test");

        // When
        Notification notification = Notification.builder()
                .notificationId("NOTIF_COMPLETE")
                .userId(100L)
                .title("Complete Notification")
                .message("This is a complete notification")
                .type(NotificationType.SECURITY_ALERT)
                .status(NotificationStatus.DELIVERED)
                .readAt(now)
                .sentAt(now)
                .deliveryMethod(DeliveryMethod.PUSH_NOTIFICATION)
                .recipientEmail("complete@example.com")
                .recipientPhone("+9876543210")
                .deviceToken("complete_device_token")
                .templateId("complete_template")
                .metadata(metadata)
                .retryCount(2)
                .maxRetries(5)
                .nextRetryAt(now.plusHours(1))
                .build();

        // Then
        assertThat(notification.getNotificationId()).isEqualTo("NOTIF_COMPLETE");
        assertThat(notification.getUserId()).isEqualTo(100L);
        assertThat(notification.getTitle()).isEqualTo("Complete Notification");
        assertThat(notification.getMessage()).isEqualTo("This is a complete notification");
        assertThat(notification.getType()).isEqualTo(NotificationType.SECURITY_ALERT);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        assertThat(notification.getReadAt()).isEqualTo(now);
        assertThat(notification.getSentAt()).isEqualTo(now);
        assertThat(notification.getDeliveryMethod()).isEqualTo(DeliveryMethod.PUSH_NOTIFICATION);
        assertThat(notification.getRecipientEmail()).isEqualTo("complete@example.com");
        assertThat(notification.getRecipientPhone()).isEqualTo("+9876543210");
        assertThat(notification.getDeviceToken()).isEqualTo("complete_device_token");
        assertThat(notification.getTemplateId()).isEqualTo("complete_template");
        assertThat(notification.getMetadata()).containsEntry("source", "test");
        assertThat(notification.getRetryCount()).isEqualTo(2);
        assertThat(notification.getMaxRetries()).isEqualTo(5);
        assertThat(notification.getNextRetryAt()).isEqualTo(now.plusHours(1));
    }
}
