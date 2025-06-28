package com.maelcolium.telepesa.notification.mapper;

import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NotificationMapperTest {

    @InjectMocks
    private NotificationMapper notificationMapper;

    private Notification notification;
    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        notification = Notification.builder()
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .recipientPhone("+254700000000")
                .metadata(Map.of("key", "value"))
                .sentAt(null)
                .readAt(null)
                .retryCount(0)
                .maxRetries(3)
                .build();

        notificationDto = NotificationDto.builder()
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .recipientPhone("+254700000000")
                .metadata(Map.of("key", "value"))
                .sentAt(null)
                .readAt(null)
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    @Test
    void toDto_WithValidNotification_ShouldReturnCorrectDto() {
        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(notification.getId());
        assertThat(result.getNotificationId()).isEqualTo(notification.getNotificationId());
        assertThat(result.getUserId()).isEqualTo(notification.getUserId());
        assertThat(result.getTitle()).isEqualTo(notification.getTitle());
        assertThat(result.getMessage()).isEqualTo(notification.getMessage());
        assertThat(result.getType()).isEqualTo(notification.getType());
        assertThat(result.getStatus()).isEqualTo(notification.getStatus());
        assertThat(result.getDeliveryMethod()).isEqualTo(notification.getDeliveryMethod());
        assertThat(result.getRecipientEmail()).isEqualTo(notification.getRecipientEmail());
        assertThat(result.getRecipientPhone()).isEqualTo(notification.getRecipientPhone());
        assertThat(result.getMetadata()).isEqualTo(notification.getMetadata());
        assertThat(result.getSentAt()).isEqualTo(notification.getSentAt());
        assertThat(result.getReadAt()).isEqualTo(notification.getReadAt());
        assertThat(result.getRetryCount()).isEqualTo(notification.getRetryCount());
        assertThat(result.getMaxRetries()).isEqualTo(notification.getMaxRetries());
        assertThat(result.getCreatedAt()).isEqualTo(notification.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(notification.getUpdatedAt());
    }

    @Test
    void toDto_WithNullNotification_ShouldReturnNull() {
        // When
        NotificationDto result = notificationMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDto_WithNullValues_ShouldHandleNullsCorrectly() {
        // Given
        Notification notificationWithNulls = Notification.builder()
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message(null)
                .type(NotificationType.TRANSACTION_SUCCESS)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail(null)
                .recipientPhone(null)
                .metadata(null)
                .sentAt(null)
                .readAt(null)
                .retryCount(0)
                .maxRetries(3)
                .build();

        // When
        NotificationDto result = notificationMapper.toDto(notificationWithNulls);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNotificationId()).isEqualTo("NOTIF-12345678");
        assertThat(result.getMessage()).isNull();
        assertThat(result.getRecipientEmail()).isNull();
        assertThat(result.getRecipientPhone()).isNull();
        assertThat(result.getMetadata()).isNull();
        assertThat(result.getSentAt()).isNull();
        assertThat(result.getReadAt()).isNull();
    }

    @Test
    void toDto_WithDifferentNotificationTypes_ShouldMapCorrectly() {
        // Test all notification types
        NotificationType[] types = NotificationType.values();
        
        for (NotificationType type : types) {
            // Given
            notification.setType(type);
            
            // When
            NotificationDto result = notificationMapper.toDto(notification);
            
            // Then
            assertThat(result.getType()).isEqualTo(type);
        }
    }

    @Test
    void toDto_WithDifferentStatuses_ShouldMapCorrectly() {
        // Test all notification statuses
        NotificationStatus[] statuses = NotificationStatus.values();
        
        for (NotificationStatus status : statuses) {
            // Given
            notification.setStatus(status);
            
            // When
            NotificationDto result = notificationMapper.toDto(notification);
            
            // Then
            assertThat(result.getStatus()).isEqualTo(status);
        }
    }

    @Test
    void toDto_WithDifferentDeliveryMethods_ShouldMapCorrectly() {
        // Test all delivery methods
        DeliveryMethod[] methods = DeliveryMethod.values();
        
        for (DeliveryMethod method : methods) {
            // Given
            notification.setDeliveryMethod(method);
            
            // When
            NotificationDto result = notificationMapper.toDto(notification);
            
            // Then
            assertThat(result.getDeliveryMethod()).isEqualTo(method);
        }
    }

    @Test
    void toDto_WithEmailDelivery_ShouldMapEmailFields() {
        // Given
        notification.setDeliveryMethod(DeliveryMethod.EMAIL);
        notification.setRecipientEmail("user@example.com");
        notification.setRecipientPhone(null);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getDeliveryMethod()).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(result.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(result.getRecipientPhone()).isNull();
    }

    @Test
    void toDto_WithSMSDelivery_ShouldMapPhoneFields() {
        // Given
        notification.setDeliveryMethod(DeliveryMethod.SMS);
        notification.setRecipientPhone("+254700000000");
        notification.setRecipientEmail(null);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getDeliveryMethod()).isEqualTo(DeliveryMethod.SMS);
        assertThat(result.getRecipientPhone()).isEqualTo("+254700000000");
        assertThat(result.getRecipientEmail()).isNull();
    }

    @Test
    void toDto_WithPushDelivery_ShouldMapCorrectly() {
        // Given
        notification.setDeliveryMethod(DeliveryMethod.PUSH_NOTIFICATION);
        notification.setRecipientEmail(null);
        notification.setRecipientPhone(null);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getDeliveryMethod()).isEqualTo(DeliveryMethod.PUSH_NOTIFICATION);
        assertThat(result.getRecipientEmail()).isNull();
        assertThat(result.getRecipientPhone()).isNull();
    }

    @Test
    void toDto_WithComplexMetadata_ShouldMapCorrectly() {
        // Given
        Map<String, String> complexMetadata = Map.of(
            "transactionId", "TXN-12345678",
            "amount", "1000.00",
            "currency", "KES",
            "timestamp", "2023-12-25T14:30:00",
            "deviceId", "device-123",
            "location", "Nairobi, Kenya"
        );
        notification.setMetadata(complexMetadata);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getMetadata()).isEqualTo(complexMetadata);
        assertThat(result.getMetadata()).containsKey("transactionId");
        assertThat(result.getMetadata()).containsKey("amount");
        assertThat(result.getMetadata()).containsKey("currency");
        assertThat(result.getMetadata()).containsKey("timestamp");
        assertThat(result.getMetadata()).containsKey("deviceId");
        assertThat(result.getMetadata()).containsKey("location");
    }

    @Test
    void toDto_WithEmptyMetadata_ShouldMapCorrectly() {
        // Given
        notification.setMetadata(Map.of());

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getMetadata()).isEmpty();
    }

    @Test
    void toDto_WithDateTimeValues_ShouldMapCorrectly() {
        // Given
        LocalDateTime sentAt = LocalDateTime.of(2023, 12, 25, 14, 31, 0);
        LocalDateTime readAt = LocalDateTime.of(2023, 12, 25, 14, 32, 0);
        
        notification.setSentAt(sentAt);
        notification.setReadAt(readAt);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getSentAt()).isEqualTo(sentAt);
        assertThat(result.getReadAt()).isEqualTo(readAt);
    }

    @Test
    void toDto_WithRetryCounts_ShouldMapCorrectly() {
        // Given
        notification.setRetryCount(3);
        notification.setMaxRetries(5);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getRetryCount()).isEqualTo(3);
        assertThat(result.getMaxRetries()).isEqualTo(5);
    }

    @Test
    void toDto_WithZeroRetryCounts_ShouldMapCorrectly() {
        // Given
        notification.setRetryCount(0);
        notification.setMaxRetries(0);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getRetryCount()).isEqualTo(0);
        assertThat(result.getMaxRetries()).isEqualTo(0);
    }

    @Test
    void toDto_WithLongValues_ShouldMapCorrectly() {
        // Given
        notification.setUserId(Long.MAX_VALUE);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getUserId()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    void toDto_WithNegativeValues_ShouldMapCorrectly() {
        // Given
        notification.setUserId(-1L);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getUserId()).isEqualTo(-1L);
    }

    @Test
    void toDto_WithSpecialCharacters_ShouldMapCorrectly() {
        // Given
        notification.setTitle("Alert: Transaction @#$%^&*() Successful!");
        notification.setMessage("Your payment of $1,000.00 was processed successfully. 🎉");

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getTitle()).isEqualTo("Alert: Transaction @#$%^&*() Successful!");
        assertThat(result.getMessage()).isEqualTo("Your payment of $1,000.00 was processed successfully. 🎉");
    }

    @Test
    void toDto_WithUnicodeCharacters_ShouldMapCorrectly() {
        // Given
        notification.setTitle("通知: 交易成功");
        notification.setMessage("您的付款已成功处理。感谢您的使用！");

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getTitle()).isEqualTo("通知: 交易成功");
        assertThat(result.getMessage()).isEqualTo("您的付款已成功处理。感谢您的使用！");
    }

    @Test
    void toDto_WithEmptyStrings_ShouldMapCorrectly() {
        // Given
        notification.setTitle("");
        notification.setMessage("");

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getTitle()).isEqualTo("");
        assertThat(result.getMessage()).isEqualTo("");
    }

    @Test
    void toDto_WithWhitespaceStrings_ShouldMapCorrectly() {
        // Given
        notification.setTitle("   ");
        notification.setMessage("  Test message  ");

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getTitle()).isEqualTo("   ");
        assertThat(result.getMessage()).isEqualTo("  Test message  ");
    }

    @Test
    void toDto_WithVeryLongStrings_ShouldMapCorrectly() {
        // Given
        String longTitle = "A".repeat(1000);
        String longMessage = "B".repeat(5000);
        notification.setTitle(longTitle);
        notification.setMessage(longMessage);

        // When
        NotificationDto result = notificationMapper.toDto(notification);

        // Then
        assertThat(result.getTitle()).isEqualTo(longTitle);
        assertThat(result.getMessage()).isEqualTo(longMessage);
    }

    @Test
    void toDto_WithAllStatusTransitions_ShouldMapCorrectly() {
        // Test different status combinations with timestamps
        LocalDateTime now = LocalDateTime.now();
        
        // PENDING -> SENT
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(now);
        NotificationDto result1 = notificationMapper.toDto(notification);
        assertThat(result1.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(result1.getSentAt()).isEqualTo(now);
        
        // SENT -> DELIVERED
        notification.setStatus(NotificationStatus.DELIVERED);
        NotificationDto result2 = notificationMapper.toDto(notification);
        assertThat(result2.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
        
        // DELIVERED -> READ
        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(now.plusMinutes(1));
        NotificationDto result3 = notificationMapper.toDto(notification);
        assertThat(result3.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(result3.getReadAt()).isEqualTo(now.plusMinutes(1));
    }
} 