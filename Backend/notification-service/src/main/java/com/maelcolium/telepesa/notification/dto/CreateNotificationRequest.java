package com.maelcolium.telepesa.notification.dto;

import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotificationRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Delivery method is required")
    private DeliveryMethod deliveryMethod;

    private String recipientEmail;

    private String recipientPhone;

    private String deviceToken;

    private String templateId;

    private Map<String, String> metadata; // Map for additional data
} 