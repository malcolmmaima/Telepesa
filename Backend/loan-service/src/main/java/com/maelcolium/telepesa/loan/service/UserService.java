package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.models.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to interact with the User Service
 */
@Service
@Slf4j
public class UserService {

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserService(RestTemplate restTemplate, 
                      @Value("${app.services.user-service.url:http://localhost:8081}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    /**
     * Get user by username from the user service
     */
    public UserDto getUserByUsername(String username) {
        try {
            String url = userServiceUrl + "/api/v1/users/internal/username/" + username;
            log.info("Calling user service: {} for username: {}", url, username);
            UserDto user = restTemplate.getForObject(url, UserDto.class);
            log.info("User service response: {}", user != null ? "User found with ID " + user.getId() : "User not found");
            return user;
        } catch (RestClientException e) {
            log.error("Failed to get user by username '{}' from user service: {}", username, e.getMessage(), e);
            return null;
        }
    }
}
