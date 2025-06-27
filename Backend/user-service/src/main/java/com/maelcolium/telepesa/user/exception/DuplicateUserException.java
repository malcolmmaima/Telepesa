package com.maelcolium.telepesa.user.exception;

import com.maelcolium.telepesa.exceptions.BusinessException;

/**
 * Exception thrown when attempting to create a user with duplicate unique fields
 */
public class DuplicateUserException extends BusinessException {
    
    public DuplicateUserException(String message) {
        super(message);
    }
    
    public DuplicateUserException(String field, String value) {
        super(String.format("User already exists with %s: %s", field, value));
    }
} 