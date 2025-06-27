package com.maelcolium.telepesa.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maelcolium.telepesa.exceptions.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationEntryPoint
 * Tests error handling and JSON response generation
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private ByteArrayOutputStream outputStream;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();
        outputStream = new ByteArrayOutputStream();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users/profile");
    }

    @Test
    void commence_WithAuthenticationException_ShouldReturnUnauthorizedResponse() throws Exception {
        // Given
        when(authException.getMessage()).thenReturn("Invalid token");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String responseContent = outputStream.toString();
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getMessage()).isEqualTo("Authentication required to access this resource");
        assertThat(errorResponse.getPath()).isEqualTo("/api/users/profile");
        assertThat(errorResponse.getTimestamp()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void commence_WithNullMessage_ShouldHandleGracefully() throws Exception {
        // Given
        when(authException.getMessage()).thenReturn(null);

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        String responseContent = outputStream.toString();
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getMessage()).isEqualTo("Authentication required to access this resource");
    }

    @Test
    void commence_ShouldUseCorrectHttpStatus() throws Exception {
        // Given
        when(authException.getMessage()).thenReturn("Token expired");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void commence_ShouldWriteValidJsonResponse() throws Exception {
        // Given
        when(authException.getMessage()).thenReturn("Authentication failed");

        // When
        jwtAuthenticationEntryPoint.commence(request, response, authException);

        // Then
        String responseContent = outputStream.toString();
        
        // Verify it's valid JSON by parsing it
        ErrorResponse errorResponse = objectMapper.readValue(responseContent, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getStatus()).isEqualTo(401);
        assertThat(errorResponse.getError()).isEqualTo("Unauthorized");
        assertThat(errorResponse.getMessage()).isEqualTo("Authentication required to access this resource");
    }

    // Mock ServletOutputStream implementation
    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        public MockServletOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
            // Not needed for test
        }
    }
}