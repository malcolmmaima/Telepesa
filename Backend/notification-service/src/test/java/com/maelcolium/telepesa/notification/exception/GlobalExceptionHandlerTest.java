package com.maelcolium.telepesa.notification.exception;

import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notifications");
    }

    @Test
    void handleResourceNotFound_ShouldReturnNotFoundResponse() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Notification not found");

        // When
        ResponseEntity<Map<String, Object>> response = 
                globalExceptionHandler.handleResourceNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("error", "Not Found");
        assertThat(response.getBody()).containsEntry("message", "Notification not found");
        assertThat(response.getBody()).containsEntry("path", "/api/v1/notifications");
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("createNotificationRequest", "title", "Title is required");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<Map<String, Object>> response = 
                globalExceptionHandler.handleValidationExceptions(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("error", "Validation Failed");
        assertThat(response.getBody()).containsEntry("message", "Invalid input data");
        assertThat(response.getBody()).containsKey("validationErrors");
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) response.getBody().get("validationErrors");
        assertThat(validationErrors).containsEntry("title", "Title is required");
    }

    @Test
    void handleGeneral_ShouldReturnInternalServerErrorResponse() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        ResponseEntity<Map<String, Object>> response = 
                globalExceptionHandler.handleGeneral(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("error", "Internal Server Error");
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
        assertThat(response.getBody()).containsEntry("path", "/api/v1/notifications");
        assertThat(response.getBody()).containsKey("timestamp");
    }
}
