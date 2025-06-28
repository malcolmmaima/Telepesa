package com.maelcolium.telepesa.notification.service;

import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.mapper.NotificationMapper;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.Notification;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.repository.NotificationRepository;
import com.maelcolium.telepesa.notification.service.impl.NotificationServiceImpl;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private CreateNotificationRequest createRequest;
    private Notification savedNotification;
    private NotificationDto notificationDto;

    @BeforeEach
    void setUp() {
        createRequest = CreateNotificationRequest.builder()
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to Telepesa!")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("user@example.com")
                .build();

        savedNotification = Notification.builder()
                .notificationId("NOTIF_001")
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to Telepesa!")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .status(NotificationStatus.PENDING)
                .recipientEmail("user@example.com")
                .retryCount(0)
                .maxRetries(3)
                .build();

        notificationDto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF_001")
                .userId(1L)
                .type(NotificationType.WELCOME_MESSAGE)
                .title("Welcome")
                .message("Welcome to Telepesa!")
                .deliveryMethod(DeliveryMethod.EMAIL)
                .status(NotificationStatus.PENDING)
                .recipientEmail("user@example.com")
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .maxRetries(3)
                .build();
    }

    @Test
    void createNotification_WithValidRequest_ShouldReturnNotificationDto() {
        // Given
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.createNotification(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper, times(2)).toDto(savedNotification); // Called twice: once for sendNotification, once for return
    }

    @Test
    void getNotification_WithExistingId_ShouldReturnNotificationDto() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.getNotification(1L);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findById(1L);
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getNotification_WithNonExistingId_ShouldThrowException() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.getNotification(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found with id: 1");

        verify(notificationRepository).findById(1L);
        verify(notificationMapper, never()).toDto(any());
    }

    @Test
    void updateNotificationStatus_WithValidIdAndStatus_ShouldReturnUpdatedNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.updateNotificationStatus(1L, NotificationStatus.SENT);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void deleteNotification_WithValidId_ShouldDeleteNotification() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));

        // When
        notificationService.deleteNotification(1L);

        // Then
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).delete(savedNotification);
    }
}
