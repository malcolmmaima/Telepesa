package com.maelcolium.telepesa.exceptions;

/**
 * Base class for all business-related exceptions in the Telepesa application.
 * This serves as the parent for domain-specific exceptions.
 */
public abstract class BusinessException extends RuntimeException {
    
    protected BusinessException(String message) {
        super(message);
    }
    
    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 