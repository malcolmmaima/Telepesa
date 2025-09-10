package com.maelcolium.telepesa.transaction.exception;

import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleResourceNotFound_ShouldReturnNotFoundResponse() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Transaction not found");

        // When
        ResponseEntity<com.maelcolium.telepesa.exceptions.ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        
        com.maelcolium.telepesa.exceptions.ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(404);
        assertThat(body.getError()).isEqualTo("Resource Not Found");
        assertThat(body.getMessage()).isEqualTo("Transaction not found");
        assertThat(body.getTimestamp()).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestResponse() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        // When
        ResponseEntity<com.maelcolium.telepesa.exceptions.ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        com.maelcolium.telepesa.exceptions.ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Validation Failed");
        assertThat(body.getMessage()).isEqualTo("Invalid input data");
        assertThat(body.getTimestamp()).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void handleGeneral_ShouldReturnInternalServerErrorResponse() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<com.maelcolium.telepesa.exceptions.ErrorResponse> response = globalExceptionHandler.handleGeneral(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        
        com.maelcolium.telepesa.exceptions.ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getMessage()).isEqualTo("Unexpected error occurred");
        assertThat(body.getTimestamp()).isInstanceOf(LocalDateTime.class);
    }
}
