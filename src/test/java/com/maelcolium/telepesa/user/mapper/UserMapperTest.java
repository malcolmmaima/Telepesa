package com.maelcolium.telepesa.user.mapper;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserMapper
 * Tests all mapping methods for complete coverage
 */
class UserMapperTest {

    private UserMapper userMapper;
    private User testUser;
    private CreateUserRequest testCreateRequest;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
        testDateTime = LocalDateTime.now();
        
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        testCreateRequest = CreateUserRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("SecurePass123!")
                .firstName("New")
                .lastName("User")
                .phoneNumber("+0987654321")
                .build();
    }

    @Test
    void toDto_WithValidUser_ShouldReturnUserDto() {
        // When
        UserDto result = userMapper.toDto(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getFirstName()).isEqualTo("Test");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhoneNumber()).isEqualTo("+1234567890");
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getCreatedAt()).isEqualTo(testDateTime);
        assertThat(result.getUpdatedAt()).isEqualTo(testDateTime);
    }

    @Test
    void toDto_WithNullUser_ShouldReturnNull() {
        // When
        UserDto result = userMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithValidRequest_ShouldReturnUser() {
        // When
        User result = userMapper.toEntity(testCreateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull(); // New entity should not have ID
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getFirstName()).isEqualTo("New");
        assertThat(result.getLastName()).isEqualTo("User");
        assertThat(result.getPhoneNumber()).isEqualTo("+0987654321");
    }

    @Test
    void toEntity_WithNullRequest_ShouldReturnNull() {
        // When
        User result = userMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void updateUserFromRequest_WithValidData_ShouldUpdateUser() {
        // Given
        User existingUser = User.builder()
                .id(1L)
                .username("olduser")
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .phoneNumber("+1111111111")
                .status(UserStatus.ACTIVE)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // When
        userMapper.updateUserFromRequest(testCreateRequest, existingUser);

        // Then
        assertThat(existingUser.getId()).isEqualTo(1L); // ID should not change
        assertThat(existingUser.getUsername()).isEqualTo("newuser");
        assertThat(existingUser.getEmail()).isEqualTo("new@example.com");
        assertThat(existingUser.getFirstName()).isEqualTo("New");
        assertThat(existingUser.getLastName()).isEqualTo("User");
        assertThat(existingUser.getPhoneNumber()).isEqualTo("+0987654321");
        assertThat(existingUser.getStatus()).isEqualTo(UserStatus.ACTIVE); // Status should not change
    }

    @Test
    void updateUserFromRequest_WithNullRequest_ShouldNotModifyUser() {
        // Given
        User originalUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(testDateTime)
                .updatedAt(testDateTime)
                .build();

        // When
        userMapper.updateUserFromRequest(null, originalUser);

        // Then - User should remain unchanged
        assertThat(originalUser.getUsername()).isEqualTo("testuser");
        assertThat(originalUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateUserFromRequest_WithNullUser_ShouldHandleGracefully() {
        // When & Then - Should not throw exception
        userMapper.updateUserFromRequest(testCreateRequest, null);
    }
} 