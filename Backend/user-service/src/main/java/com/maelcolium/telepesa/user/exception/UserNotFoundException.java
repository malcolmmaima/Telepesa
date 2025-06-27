package com.maelcolium.telepesa.user.exception;

import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when a user is not found
 */
public class UserNotFoundException extends ResourceNotFoundException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long userId) {
        super("User", userId);
    }
    
    public UserNotFoundException(String field, String value) {
        super(String.format("User not found with %s: %s", field, value));
    }
} 