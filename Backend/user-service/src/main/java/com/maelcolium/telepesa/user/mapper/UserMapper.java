package com.maelcolium.telepesa.user.mapper;

import com.maelcolium.telepesa.models.dto.UserDto;
import com.maelcolium.telepesa.user.dto.CreateUserRequest;
import com.maelcolium.telepesa.user.model.User;
import org.springframework.stereotype.Component;

/**
 * Simple manual mapper for User entity and DTOs conversion
 * Using manual mapping instead of MapStruct to avoid compilation issues
 */
@Component
public class UserMapper {

    /**
     * Convert User entity to UserDto
     */
    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        return dto;
    }

    /**
     * Convert CreateUserRequest to User entity
     */
    public User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        return User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .phoneNumber(request.getPhoneNumber())
            .build();
    }

    /**
     * Update existing User entity with data from CreateUserRequest
     */
    public void updateUserFromRequest(CreateUserRequest request, User user) {
        if (request == null || user == null) {
            return;
        }
        
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
    }
} 