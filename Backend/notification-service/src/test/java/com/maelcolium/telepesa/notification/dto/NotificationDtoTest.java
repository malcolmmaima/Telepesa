package com.maelcolium.telepesa.notification.dto;

import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDtoTest {

    @Test
    void builder_ShouldCreateValidDto() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // When
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test Notification")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .readAt(now)
                .sentAt(now)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .recipientPhone("+1234567890")
                .deviceToken("device123")
                .templateId("template123")
                .metadata(metadata)
                .retryCount(0)
                .maxRetries(3)
                .nextRetryAt(now.plusHours(1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNotificationId()).isEqualTo("NOTIF_001");
        assertThat(dto.getUserId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("Test Notification");
        assertThat(dto.getMessage()).isEqualTo("Test message");
        assertThat(dto.getType()).isEqualTo(NotificationType.WELCOME_MESSAGE);
        assertThat(dto.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(dto.getReadAt()).isEqualTo(now);
        assertThat(dto.getSentAt()).isEqualTo(now);
        assertThat(dto.getDeliveryMethod()).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(dto.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(dto.getRecipientPhone()).isEqualTo("+1234567890");
        assertThat(dto.getDeviceToken()).isEqualTo("device123");
        assertThat(dto.getTemplateId()).isEqualTo("template123");
        assertThat(dto.getMetadata()).hasSize(2);
        assertThat(dto.getMetadata().get("key1")).isEqualTo("value1");
        assertThat(dto.getRetryCount()).isEqualTo(0);
        assertThat(dto.getMaxRetries()).isEqualTo(3);
        assertThat(dto.getNextRetryAt()).isEqualTo(now.plusHours(1));
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Given
        NotificationDto dto1 = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        NotificationDto dto2 = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toString_ShouldReturnStringRepresentation() {
        // Given
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(1L)
                .title("Test")
                .message("Test message")
                .type(NotificationType.WELCOME_MESSAGE)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("NOTIF_001");
        assertThat(result).contains("Test");
        assertThat(result).contains("1");
    }

    @Test
    void gettersAndSetters_ShouldWorkCorrectly() {
        // Given
        NotificationDto dto = new NotificationDto();
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> metadata = new HashMap<>();
        metadata.put("testKey", "testValue");

        // When
        dto.setId(100L);
        dto.setNotificationId("NOTIF_100");
        dto.setUserId(100L);
        dto.setTitle("Updated Title");
        dto.setMessage("Updated message");
        dto.setType(NotificationType.TRANSACTION_SUCCESS);
        dto.setStatus(NotificationStatus.SENT);
        dto.setReadAt(now);
        dto.setSentAt(now);
        dto.setDeliveryMethod(DeliveryMethod.SMS);
        dto.setRecipientEmail("updated@example.com");
        dto.setRecipientPhone("+9876543210");
        dto.setDeviceToken("updated_device");
        dto.setTemplateId("updated_template");
        dto.setMetadata(metadata);
        dto.setRetryCount(2);
        dto.setMaxRetries(5);
        dto.setNextRetryAt(now.plusHours(2));
        dto.setCreatedAt(now);
        dto.setUpdatedAt(now.plusMinutes(30));

        // Then
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getNotificationId()).isEqualTo("NOTIF_100");
        assertThat(dto.getUserId()).isEqualTo(100L);
        assertThat(dto.getTitle()).isEqualTo("Updated Title");
        assertThat(dto.getMessage()).isEqualTo("Updated message");
        assertThat(dto.getType()).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
        assertThat(dto.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(dto.getReadAt()).isEqualTo(now);
        assertThat(dto.getSentAt()).isEqualTo(now);
        assertThat(dto.getDeliveryMethod()).isEqualTo(DeliveryMethod.SMS);
        assertThat(dto.getRecipientEmail()).isEqualTo("updated@example.com");
        assertThat(dto.getRecipientPhone()).isEqualTo("+9876543210");
        assertThat(dto.getDeviceToken()).isEqualTo("updated_device");
        assertThat(dto.getTemplateId()).isEqualTo("updated_template");
        assertThat(dto.getMetadata()).containsEntry("testKey", "testValue");
        assertThat(dto.getRetryCount()).isEqualTo(2);
        assertThat(dto.getMaxRetries()).isEqualTo(5);
        assertThat(dto.getNextRetryAt()).isEqualTo(now.plusHours(2));
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now.plusMinutes(30));
    }

    @Test
    void allNotificationTypes_ShouldBeSupported() {
        // Given & When & Then
        for (NotificationType type : NotificationType.values()) {
            NotificationDto dto = NotificationDto.builder()
                    .id(1L)
                    .notificationId("NOTIF_001")
                    .userId(123L)
                    .type(type)
                    .title("Test")
                    .message("Test message")
                    .status(NotificationStatus.PENDING)
                    .deliveryMethod(DeliveryMethod.EMAIL)
                    .createdAt(LocalDateTime.now())
                    .build();

            assertThat(dto.getType()).isEqualTo(type);
        }
    }

    @Test
    void allNotificationStatuses_ShouldBeSupported() {
        // Given & When & Then
        for (NotificationStatus status : NotificationStatus.values()) {
            NotificationDto dto = NotificationDto.builder()
                    .id(1L)
                    .notificationId("NOTIF_001")
                    .userId(123L)
                    .type(NotificationType.WELCOME_MESSAGE)
                    .title("Test")
                    .message("Test message")
                    .status(status)
                    .deliveryMethod(DeliveryMethod.EMAIL)
                    .createdAt(LocalDateTime.now())
                    .build();

            assertThat(dto.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void allDeliveryMethods_ShouldBeSupported() {
        // Given & When & Then
        for (DeliveryMethod method : DeliveryMethod.values()) {
            NotificationDto dto = NotificationDto.builder()
                    .id(1L)
                    .notificationId("NOTIF_001")
                    .userId(123L)
                    .type(NotificationType.WELCOME_MESSAGE)
                    .title("Test")
                    .message("Test message")
                    .status(NotificationStatus.PENDING)
                    .deliveryMethod(method)
                    .createdAt(LocalDateTime.now())
                    .build();

            assertThat(dto.getDeliveryMethod()).isEqualTo(method);
        }
    }

    @Test
    void minimalDto_ShouldWorkCorrectly() {
        // Given & When
        NotificationDto dto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(123L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome message")
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .createdAt(LocalDateTime.now())
                .build();

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getNotificationId()).isEqualTo("NOTIF_001");
        assertThat(dto.getUserId()).isEqualTo(123L);
        assertThat(dto.getType()).isEqualTo(NotificationType.WELCOME_MESSAGE);
        assertThat(dto.getTitle()).isEqualTo("Welcome");
        assertThat(dto.getMessage()).isEqualTo("Welcome message");
        assertThat(dto.getStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(dto.getDeliveryMethod()).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(dto.getCreatedAt()).isNotNull();
        
        // Optional fields should be null
        assertThat(dto.getRecipientEmail()).isNull();
        assertThat(dto.getRecipientPhone()).isNull();
        assertThat(dto.getDeviceToken()).isNull();
        assertThat(dto.getTemplateId()).isNull();
        assertThat(dto.getSentAt()).isNull();
        assertThat(dto.getReadAt()).isNull();
        assertThat(dto.getNextRetryAt()).isNull();
        assertThat(dto.getMetadata()).isNull();
    }
} 