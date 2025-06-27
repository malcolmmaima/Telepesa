package com.maelcolium.telepesa.user.mapper;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.models.enums.UserStatus;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toDto_WithValidUser_ShouldReturnUserDto() {
        // Given
        User user = User.builder()
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

        // When
        UserDto result = userMapper.toDto(user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
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
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("SecurePass123!")
                .firstName("New")
                .lastName("User")
                .phoneNumber("+0987654321")
                .build();

        // When
        User result = userMapper.toEntity(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("newuser");
        assertThat(result.getEmail()).isEqualTo("new@example.com");
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
        User user = User.builder()
                .id(1L)
                .username("olduser")
                .email("old@example.com")
                .firstName("Old")
                .lastName("User")
                .phoneNumber("+1111111111")
                .status(UserStatus.ACTIVE)
                .build();
                
        CreateUserRequest request = CreateUserRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("SecurePass123!")
                .firstName("New")
                .lastName("User")
                .phoneNumber("+0987654321")
                .build();

        // When
        userMapper.updateUserFromRequest(request, user);

        // Then
        assertThat(user.getUsername()).isEqualTo("newuser");
        assertThat(user.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUserFromRequest_WithNullRequest_ShouldNotModifyUser() {
        // Given
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        // When
        userMapper.updateUserFromRequest(null, user);

        // Then
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
    }
}
