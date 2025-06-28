package com.maelcolium.telepesa.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import com.maelcolium.telepesa.notification.dto.CreateNotificationRequest;
import com.maelcolium.telepesa.notification.dto.NotificationDto;
import com.maelcolium.telepesa.notification.exception.GlobalExceptionHandler;
import com.maelcolium.telepesa.notification.model.DeliveryMethod;
import com.maelcolium.telepesa.notification.model.NotificationStatus;
import com.maelcolium.telepesa.notification.model.NotificationType;
import com.maelcolium.telepesa.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.HashMap;
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
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();

        // Setup test data
        Map<String, String> metadata = new HashMap<>();
        metadata.put("priority", "high");
        metadata.put("category", "banking");

        notificationDto = NotificationDto.builder()
                .id(1L)
                .notificationId("NOTIF-12345678")
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.ACCOUNT_UPDATED)
                .status(NotificationStatus.PENDING)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .metadata(metadata)
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateNotificationRequest.builder()
                .userId(10L)
                .title("Test Notification")
                .message("This is a test notification")
                .type(NotificationType.ACCOUNT_UPDATED)
                .deliveryMethod(DeliveryMethod.EMAIL)
                .metadata(metadata)
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
                .andExpect(jsonPath("$.userId").value(10))
                .andExpect(jsonPath("$.title").value("Test Notification"));

        verify(notificationService).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    void createNotification_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateNotificationRequest invalidRequest = CreateNotificationRequest.builder().build();

        // When & Then
        mockMvc.perform(post("/api/v1/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).createNotification(any(CreateNotificationRequest.class));
    }

    @Test
    void getNotification_WithValidId_ShouldReturnNotification() throws Exception {
        // Given
        when(notificationService.getNotification(1L)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"))
                .andExpect(jsonPath("$.userId").value(10));

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
        mockMvc.perform(get("/api/v1/notifications/notification-id/NOTIF-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationId").value("NOTIF-12345678"));

        verify(notificationService).getNotificationByNotificationId("NOTIF-12345678");
    }

    @Test
    void getNotifications_ShouldReturnPagedResults() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotifications(pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].notificationId").value("NOTIF-12345678"));

        verify(notificationService).getNotifications(pageRequest);
    }

    @Test
    void getNotificationsByUserId_ShouldReturnUserNotifications() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotificationsByUserId(10L, pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(10));

        verify(notificationService).getNotificationsByUserId(10L, pageRequest);
    }

    @Test
    void getNotificationsByStatus_ShouldReturnStatusFilteredNotifications() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotificationsByStatus(NotificationStatus.PENDING, pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/status/PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByStatus(NotificationStatus.PENDING, pageRequest);
    }

    @Test
    void getNotificationsByType_ShouldReturnTypeFilteredNotifications() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotificationsByType(NotificationType.ACCOUNT_UPDATED, pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/type/ACCOUNT_UPDATED")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByType(NotificationType.ACCOUNT_UPDATED, pageRequest);
    }

    @Test
    void getNotificationsByDeliveryMethod_ShouldReturnMethodFilteredNotifications() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotificationsByDeliveryMethod(DeliveryMethod.EMAIL, pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/delivery-method/EMAIL")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByDeliveryMethod(DeliveryMethod.EMAIL, pageRequest);
    }

    @Test
    void getNotificationsByDateRange_ShouldReturnDateFilteredNotifications() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getNotificationsByDateRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageRequest))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10/date-range")
                        .param("startDate", "2023-12-01")
                        .param("endDate", "2023-12-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getNotificationsByDateRange(eq(10L), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageRequest));
    }

    @Test
    void updateNotificationStatus_WithValidRequest_ShouldReturnUpdatedNotification() throws Exception {
        // Given
        when(notificationService.updateNotificationStatus(1L, NotificationStatus.SENT)).thenReturn(notificationDto);

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/1/status")
                        .param("status", "SENT"))
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
                        .param("status", "SENT"))
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
        when(notificationService.getUnreadNotificationsByUserId(10L)).thenReturn(List.of(notificationDto));

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/user/10/unread"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].notificationId").value("NOTIF-12345678"));

        verify(notificationService).getUnreadNotificationsByUserId(10L);
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
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NotificationDto> page = new PageImpl<>(List.of(notificationDto), pageRequest, 1);
        when(notificationService.getFailedNotifications(pageRequest)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/failed")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(notificationService).getFailedNotifications(pageRequest);
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
        mockMvc.perform(get("/api/v1/notifications/user/10/count-by-status")
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