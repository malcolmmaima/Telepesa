package com.maelcolium.telepesa.notification.dto;

import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationType;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateNotificationRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    void builder_ShouldCreateValidRequest() {
        // Given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "value1");
        metadata.put("key2", "value2");

        // When
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .recipientPhone("+1234567890")
                .deviceToken("device123")
                .templateId("template1")
                .metadata(metadata)
                .build();

        // Then
        assertThat(request.getUserId()).isEqualTo(1L);
        assertThat(request.getType()).isEqualTo(NotificationType.WELCOME_MESSAGE);
        assertThat(request.getTitle()).isEqualTo("Welcome");
        assertThat(request.getMessage()).isEqualTo("Welcome to our service");
        assertThat(request.getDeliveryMethod()).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(request.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(request.getRecipientPhone()).isEqualTo("+1234567890");
        assertThat(request.getDeviceToken()).isEqualTo("device123");
        assertThat(request.getTemplateId()).isEqualTo("template1");
        assertThat(request.getMetadata()).hasSize(2);
        assertThat(request.getMetadata()).containsEntry("key1", "value1");
        assertThat(request.getMetadata()).containsEntry("key2", "value2");
    }

    @Test
    void validation_WithValidData_ShouldPassValidation() {
        // Given
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<CreateNotificationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void validation_WithMissingRequiredFields_ShouldFailValidation() {
        // Given
        CreateNotificationRequest request = CreateNotificationRequest.builder().build();

        // When
        Set<ConstraintViolation<CreateNotificationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).hasSize(5); // userId, type, title, message, deliveryMethod are required
    }

    @Test
    void validation_WithExcessivelyLongTitle_ShouldFailValidation() {
        // Given
        String longTitle = "A".repeat(201); // Exceeds 200 character limit
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title(longTitle)
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<CreateNotificationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Title must not exceed 200 characters");
    }

    @Test
    void validation_WithExcessivelyLongMessage_ShouldFailValidation() {
        // Given
        String longMessage = "A".repeat(1001); // Exceeds 1000 character limit
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message(longMessage)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<CreateNotificationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).contains("Message must not exceed 1000 characters");
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Given
        CreateNotificationRequest request1 = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        CreateNotificationRequest request2 = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        CreateNotificationRequest request3 = CreateNotificationRequest.builder()
                .userId(2L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .build();

        // When
        String toString = request.toString();

        // Then
        assertThat(toString).contains("userId=1");
        assertThat(toString).contains("type=WELCOME_MESSAGE");
        assertThat(toString).contains("title=Welcome");
        assertThat(toString).contains("message=Welcome to our service");
        assertThat(toString).contains("deliveryMethod=EMAIL");
        assertThat(toString).contains("recipientEmail=user@example.com");
    }

    @Test
    void optionalFields_ShouldBeNullByDefault() {
        // Given & When
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to our service")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .build();

        // Then
        assertThat(request.getRecipientEmail()).isNull();
        assertThat(request.getRecipientPhone()).isNull();
        assertThat(request.getDeviceToken()).isNull();
        assertThat(request.getTemplateId()).isNull();
        assertThat(request.getMetadata()).isNull();
    }

    @Test
    void allFieldsSet_ShouldWorkCorrectly() {
        // Given
        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");
        metadata.put("source", "system");

        // When
        CreateNotificationRequest request = CreateNotificationRequest.builder()
                .userId(123L)
                .type(NotificationType.TRANSACTION_SUCCESS)
                .title("Transaction Completed")
                .message("Your transaction has been completed successfully")
                .deliveryMethod(DeliveryMethod.PUSH_NOTIFICATION)
                .recipientEmail("user@example.com")
                .recipientPhone("+1234567890")
                .deviceToken("device-token-123")
                .templateId("transaction-success-template")
                .metadata(metadata)
                .build();

        // Then
        assertThat(request.getUserId()).isEqualTo(123L);
        assertThat(request.getType()).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
        assertThat(request.getTitle()).isEqualTo("Transaction Completed");
        assertThat(request.getMessage()).isEqualTo("Your transaction has been completed successfully");
        assertThat(request.getDeliveryMethod()).isEqualTo(DeliveryMethod.PUSH_NOTIFICATION);
        assertThat(request.getRecipientEmail()).isEqualTo("user@example.com");
        assertThat(request.getRecipientPhone()).isEqualTo("+1234567890");
        assertThat(request.getDeviceToken()).isEqualTo("device-token-123");
        assertThat(request.getTemplateId()).isEqualTo("transaction-success-template");
        assertThat(request.getMetadata()).containsEntry("priority", "high");
        assertThat(request.getMetadata()).containsEntry("source", "system");
    }
} 