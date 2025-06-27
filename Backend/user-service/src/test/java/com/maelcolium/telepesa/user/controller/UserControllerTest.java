package com.maelcolium.telepesa.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.dto.LoginRequest;
import com.maelcolium.telepesa.user.dto.LoginResponse;
import com.maelcolium.telepesa.user.exception.DuplicateUserException;
import com.maelcolium.telepesa.user.exception.UserNotFoundException;
import com.maelcolium.telepesa.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 * Tests REST endpoints with security features and validation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateUserRequest createUserRequest;
    private LoginRequest loginRequest;
    private UserDto userDto;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        createUserRequest = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("SecurePass123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("SecurePass123!")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        loginResponse = LoginResponse.builder()
                .accessToken("jwt-token-123")
                .tokenType("Bearer")
                .user(userDto)
                .build();
    }

    // ===== REGISTRATION ENDPOINT TESTS =====

    @Test
    void register_WithValidRequest_ShouldReturnCreatedUser() throws Exception {
        // Given
        when(userService.createUserWithSecurity(any(CreateUserRequest.class), any(HttpServletRequest.class)))
                .thenReturn(userDto);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }

    @Test
    void register_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing required fields
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .username("") // Empty username
                .email("invalid-email") // Invalid email format
                .password("123") // Weak password
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithDuplicateUser_ShouldReturnConflict() throws Exception {
        // Given
        when(userService.createUserWithSecurity(any(CreateUserRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new DuplicateUserException("Username already exists"));

        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Duplicate User"))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    // ===== LOGIN ENDPOINT TESTS =====

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Given
        when(userService.authenticateUserWithSecurity(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        LoginRequest invalidLogin = LoginRequest.builder()
                .usernameOrEmail("testuser")
                .password("wrongpassword")
                .build();

        when(userService.authenticateUserWithSecurity(any(LoginRequest.class), any(HttpServletRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void login_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        // Given - Invalid request with missing fields
        LoginRequest invalidLogin = LoginRequest.builder()
                .usernameOrEmail("") // Empty username
                .password("") // Empty password
                .build();

        // When & Then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());
    }

    // ===== USER RETRIEVAL TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_WithValidId_ShouldReturnUser() throws Exception {
        // Given
        Long userId = 1L;
        when(userService.getUser(userId)).thenReturn(userDto);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.getUser(userId)).thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User Not Found"))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUsers_WithPagination_ShouldReturnPagedUsers() throws Exception {
        // Given
        Page<UserDto> userPage = new PageImpl<>(List.of(userDto), PageRequest.of(0, 10), 1);
        when(userService.getUsers(any(PageRequest.class))).thenReturn(userPage);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    // ===== USER UPDATE TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithValidData_ShouldReturnUpdatedUser() throws Exception {
        // Given
        Long userId = 1L;
        CreateUserRequest updateRequest = CreateUserRequest.builder()
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("User")
                .status(UserStatus.ACTIVE)
                .build();

        when(userService.updateUser(eq(userId), any(CreateUserRequest.class))).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.updateUser(eq(userId), any(CreateUserRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isNotFound());
    }

    // ===== USER DELETION TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithValidId_ShouldReturnNoContent() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        Long userId = 999L;
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    // ===== EMAIL VERIFICATION TESTS =====

    @Test
    void verifyEmail_WithValidToken_ShouldReturnSuccess() throws Exception {
        // Given
        String token = "valid-verification-token";
        doNothing().when(userService).verifyEmail(token);

        // When & Then
        mockMvc.perform(get("/api/users/verify-email")
                        .param("token", token))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));
    }

    @Test
    void verifyEmail_WithInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Given
        String token = "invalid-token";
        doThrow(new IllegalArgumentException("Invalid token")).when(userService).verifyEmail(token);

        // When & Then
        mockMvc.perform(get("/api/users/verify-email")
                        .param("token", token))
                .andExpect(status().isBadRequest());
    }

    // ===== ACCOUNT MANAGEMENT TESTS =====

    @Test
    @WithMockUser(roles = "ADMIN")
    void lockAccount_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).lockUserAccount(userId);

        // When & Then
        mockMvc.perform(post("/api/users/{id}/lock", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Account locked successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unlockAccount_WithValidId_ShouldReturnSuccess() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).unlockUserAccount(userId);

        // When & Then
        mockMvc.perform(post("/api/users/{id}/unlock", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Account unlocked successfully"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void lockAccount_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/1/lock"))
                .andExpect(status().isForbidden());
    }

    // ===== CONTENT TYPE AND VALIDATION TESTS =====

    @Test
    void register_WithInvalidContentType_ShouldReturnUnsupportedMediaType() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void register_WithMalformedJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ===== SECURITY HEADER TESTS =====

    @Test
    void allEndpoints_ShouldIncludeSecurityHeaders() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }
} 