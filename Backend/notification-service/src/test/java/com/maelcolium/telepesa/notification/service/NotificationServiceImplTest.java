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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        savedNotification.setId(1L);

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
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));

        // When
        NotificationDto result = notificationService.createNotification(createRequest);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        verify(notificationMapper, atLeastOnce()).toDto(savedNotification);
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
        when(notificationRepository.existsById(1L)).thenReturn(true);

        // When
        notificationService.deleteNotification(1L);

        // Then
        verify(notificationRepository).existsById(1L);
        verify(notificationRepository).deleteById(1L);
    }

    @Test
    void deleteNotification_WithNonExistingId_ShouldThrowException() {
        // Given
        when(notificationRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> notificationService.deleteNotification(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found with id: 1");

        verify(notificationRepository).existsById(1L);
        verify(notificationRepository, never()).deleteById(any());
    }

    @Test
    void getNotificationByNotificationId_WithExistingId_ShouldReturnNotificationDto() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF_001")).thenReturn(Optional.of(savedNotification));
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.getNotificationByNotificationId("NOTIF_001");

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByNotificationId("NOTIF_001");
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getNotificationByNotificationId_WithNonExistingId_ShouldThrowException() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF_001")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> notificationService.getNotificationByNotificationId("NOTIF_001"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification not found with notification ID: NOTIF_001");

        verify(notificationRepository).findByNotificationId("NOTIF_001");
        verify(notificationMapper, never()).toDto(any());
    }

    @Test
    void getNotifications_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findAll(pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getNotifications(pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findAll(pageable);
    }

    @Test
    void getNotificationsByUserId_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findByUserId(1L, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getNotificationsByUserId(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByUserId(1L, pageable);
    }

    @Test
    void getNotificationsByStatus_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findByStatus(NotificationStatus.PENDING, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getNotificationsByStatus(NotificationStatus.PENDING, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByStatus(NotificationStatus.PENDING, pageable);
    }

    @Test
    void getNotificationsByType_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findByType(NotificationType.WELCOME_MESSAGE, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getNotificationsByType(NotificationType.WELCOME_MESSAGE, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByType(NotificationType.WELCOME_MESSAGE, pageable);
    }

    @Test
    void markAsRead_ShouldUpdateStatusToRead() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.markAsRead(1L);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void markAsReadByNotificationId_ShouldUpdateStatusToRead() {
        // Given
        when(notificationRepository.findByNotificationId("NOTIF_001")).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.markAsReadByNotificationId("NOTIF_001");

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByNotificationId("NOTIF_001");
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getUnreadNotificationsByUserId_ShouldReturnListOfNotifications() {
        // Given
        List<Notification> notifications = List.of(savedNotification);
        when(notificationRepository.findUnreadNotificationsByUserId(1L)).thenReturn(notifications);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        List<NotificationDto> result = notificationService.getUnreadNotificationsByUserId(1L);

        // Then
        assertThat(result).hasSize(1);
        verify(notificationRepository).findUnreadNotificationsByUserId(1L);
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getUnreadNotificationCount_ShouldReturnCount() {
        // Given
        when(notificationRepository.countUnreadNotificationsByUserId(1L)).thenReturn(5L);

        // When
        long result = notificationService.getUnreadNotificationCount(1L);

        // Then
        assertThat(result).isEqualTo(5L);
        verify(notificationRepository).countUnreadNotificationsByUserId(1L);
    }

    @Test
    void getReadNotificationCount_ShouldReturnCount() {
        // Given
        when(notificationRepository.countReadNotificationsByUserId(1L)).thenReturn(3L);

        // When
        long result = notificationService.getReadNotificationCount(1L);

        // Then
        assertThat(result).isEqualTo(3L);
        verify(notificationRepository).countReadNotificationsByUserId(1L);
    }

    @Test
    void getNotificationCountByUserIdAndStatus_ShouldReturnCount() {
        // Given
        when(notificationRepository.countByUserIdAndStatus(1L, NotificationStatus.PENDING)).thenReturn(2L);

        // When
        long result = notificationService.getNotificationCountByUserIdAndStatus(1L, NotificationStatus.PENDING);

        // Then
        assertThat(result).isEqualTo(2L);
        verify(notificationRepository).countByUserIdAndStatus(1L, NotificationStatus.PENDING);
    }

    @Test
    void retryFailedNotification_WithValidFailedNotification_ShouldReturnUpdatedNotification() {
        // Given
        savedNotification.setStatus(NotificationStatus.FAILED);
        savedNotification.setRetryCount(1);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        NotificationDto result = notificationService.retryFailedNotification(1L);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getPendingNotifications_ShouldReturnListOfNotifications() {
        // Given
        List<Notification> notifications = List.of(savedNotification);
        when(notificationRepository.findPendingNotificationsForRetry(eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(notifications);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        List<NotificationDto> result = notificationService.getPendingNotifications();

        // Then
        assertThat(result).hasSize(1);
        verify(notificationRepository).findPendingNotificationsForRetry(eq(NotificationStatus.PENDING), any(LocalDateTime.class));
        verify(notificationMapper).toDto(savedNotification);
    }

    @Test
    void getFailedNotifications_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findByStatus(NotificationStatus.FAILED, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getFailedNotifications(pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByStatus(NotificationStatus.FAILED, pageable);
    }

    @Test
    void getUnreadNotifications_ShouldReturnPageOfNotifications() {
        // Given
        Pageable pageable = Pageable.unpaged();
        Page<Notification> notificationPage = new PageImpl<>(List.of(savedNotification));
        when(notificationRepository.findByUserIdAndStatus(1L, NotificationStatus.PENDING, pageable)).thenReturn(notificationPage);

        // When
        Page<NotificationDto> result = notificationService.getUnreadNotifications(1L, pageable);

        // Then
        assertThat(result).isNotNull();
        verify(notificationRepository).findByUserIdAndStatus(1L, NotificationStatus.PENDING, pageable);
    }

    @Test
    void sendNotification_ShouldCallSendNotificationMethod() {
        // Given
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // When
        notificationService.sendNotification(notificationDto);

        // Then
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    void processPendingNotifications_ShouldProcessPendingNotifications() {
        // Given
        List<Notification> notifications = List.of(savedNotification);
        when(notificationRepository.findPendingNotificationsForRetry(eq(NotificationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(notifications);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(savedNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(notificationMapper.toDto(savedNotification)).thenReturn(notificationDto);

        // When
        notificationService.processPendingNotifications();

        // Then
        verify(notificationRepository).findPendingNotificationsForRetry(eq(NotificationStatus.PENDING), any(LocalDateTime.class));
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }


}
