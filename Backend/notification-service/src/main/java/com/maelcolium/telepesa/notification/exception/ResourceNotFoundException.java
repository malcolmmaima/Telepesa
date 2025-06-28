package com.maelcolium.telepesa.notification.exception;

/**
 * Exception thrown when a requested resource is not found.
 * This is a local copy to avoid shared library dependency issues.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
} 