package com.maelcolium.telepesa.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.service.NotificationService;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;
    @InjectMocks
    private NotificationController notificationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private NotificationDto notificationDto;
    private CreateNotificationRequest createRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController).build();
        objectMapper = new ObjectMapper();

        notificationDto = NotificationDto.builder()
                .id(1L)
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

        createRequest = CreateNotificationRequest.builder()
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.TRANSACTION_SUCCESS)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .recipientEmail("test@example.com")
                .recipientPhone("+254700000000")
                .metadata(Map.of("key", "value"))
                .build();
    }

    @Test
    void createNotification_WithValidRequest_ShouldReturnCreatedNotification() throws Exception {
        // Given
        when(notificationService.createNotification(any(CreateNotificationRequest.class))).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"))
                .andExpect(jsonPath("$.title").value("Test Notification"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(notificationService).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    void createNotification_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateNotificationRequest invalidRequest = CreateNotificationRequest.builder()
                .userId(null)
                .title(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNotification_WithValidId_ShouldReturnNotification() throws Exception {
        // Given
        when(notificationService.getNotification(1L)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"))
                .andExpect(jsonPath("$.title").value("Test Notification"));

        verify(notificationService).getNotification(1L);
    }

    @Test
    void getNotification_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(notificationService.getNotification(999L))
                .thenThrow(new ResourceNotFoundException("Notification not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/999"))
                .andExpect(status().isNotFound());

        verify(notificationService).getNotification(999L);
    }

    @Test
    void getNotificationByNotificationId_WithValidId_ShouldReturnNotification() throws Exception {
        // Given
        when(notificationService.getNotificationByNotificationId("NOTIF-12345678")).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/by-notification-id/NOTIF-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"));

        verify(notificationService).getNotificationByNotificationId("NOTIF-12345678");
    }

    @Test
    void getNotifications_ShouldReturnPagedResults() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotifications(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].notificationId").value("NOTIF-12345678"));

        verify(notificationService).getNotifications(any(PageRequest.class));
    }

    @Test
    void getNotificationsByUserId_ShouldReturnUserNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotificationsByUserId(10L, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(10));

        verify(notificationService).getNotificationsByUserId(10L, PageRequest.of(0, 10));
    }

    @Test
    void getNotificationsByStatus_ShouldReturnStatusFilteredNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotificationsByStatus(NotificationStatus.PENDING, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/status/PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByStatus(NotificationStatus.PENDING, PageRequest.of(0, 10));
    }

    @Test
    void getNotificationsByType_ShouldReturnTypeFilteredNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotificationsByType(NotificationType.TRANSACTION_SUCCESS, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/type/TRANSACTION_SUCCESS")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByType(NotificationType.TRANSACTION_SUCCESS, PageRequest.of(0, 10));
    }

    @Test
    void getNotificationsByDeliveryMethod_ShouldReturnMethodFilteredNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotificationsByDeliveryMethod(DeliveryMethod.EMAIL, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/delivery-method/EMAIL")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByDeliveryMethod(DeliveryMethod.EMAIL, PageRequest.of(0, 10));
    }

    @Test
    void getNotificationsByDateRange_ShouldReturnDateFilteredNotifications() throws Exception {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getNotificationsByDateRange(10L, startDate, endDate, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByDateRange(10L, startDate, endDate, PageRequest.of(0, 10));
    }

    @Test
    void updateNotificationStatus_WithValidRequest_ShouldReturnUpdatedNotification() throws Exception {
        // Given
        when(notificationService.updateNotificationStatus(1L, NotificationStatus.SENT)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"SENT\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"));

        verify(notificationService).updateNotificationStatus(1L, NotificationStatus.SENT);
    }

    @Test
    void updateNotificationStatus_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(notificationService.updateNotificationStatus(999L, NotificationStatus.SENT))
                .thenThrow(new ResourceNotFoundException("Notification not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"SENT\""))
                .andExpect(status().isNotFound());

        verify(notificationService).updateNotificationStatus(999L, NotificationStatus.SENT);
    }

    @Test
    void markAsRead_WithValidId_ShouldReturnMarkedNotification() throws Exception {
        // Given
        when(notificationService.markAsRead(1L)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"));

        verify(notificationService).markAsRead(1L);
    }

    @Test
    void markAsRead_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(notificationService.markAsRead(999L))
                .thenThrow(new ResourceNotFoundException("Notification not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/999/read"))
                .andExpect(status().isNotFound());

        verify(notificationService).markAsRead(999L);
    }

    @Test
    void getUnreadNotifications_ShouldReturnUnreadNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getUnreadNotifications(10L, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10/unread")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getUnreadNotifications(10L, PageRequest.of(0, 10));
    }

    @Test
    void getPendingNotifications_ShouldReturnPendingNotifications() throws Exception {
        // Given
        when(notificationService.getPendingNotifications()).thenReturn(List.of(notificationDto));

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value("NOTIF-12345678"));

        verify(notificationService).getPendingNotifications();
    }

    @Test
    void getFailedNotifications_ShouldReturnFailedNotifications() throws Exception {
        // Given
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto));
        when(notificationService.getFailedNotifications(PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/failed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getFailedNotifications(PageRequest.of(0, 10));
    }

    @Test
    void retryFailedNotification_WithValidId_ShouldReturnRetriedNotification() throws Exception {
        // Given
        when(notificationService.retryFailedNotification(1L)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/1/retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"));

        verify(notificationService).retryFailedNotification(1L);
    }

    @Test
    void retryFailedNotification_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(notificationService.retryFailedNotification(999L))
                .thenThrow(new ResourceNotFoundException("Notification not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/999/retry"))
                .andExpect(status().isNotFound());

        verify(notificationService).retryFailedNotification(999L);
    }

    @Test
    void getNotificationCountByUserIdAndStatus_ShouldReturnCount() throws Exception {
        // Given
        when(notificationService.getNotificationCountByUserIdAndStatus(10L, NotificationStatus.PENDING)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10/count")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(notificationService).getNotificationCountByUserIdAndStatus(10L, NotificationStatus.PENDING);
    }

    @Test
    void deleteNotification_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(notificationService).deleteNotification(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isNoContent());

        verify(notificationService).deleteNotification(1L);
    }

    @Test
    void deleteNotification_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Notification not found"))
                .when(notificationService).deleteNotification(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/notifications/999"))
                .andExpect(status().isNotFound());

        verify(notificationService).deleteNotification(999L);
    }
} 