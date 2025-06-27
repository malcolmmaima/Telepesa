package com.maelcolium.telepesa.models.enums;

/**
 * Enumeration of possible account statuses in the banking system.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public enum AccountStatus {
    
    /**
     * Account is newly created and pending verification
     */
    PENDING,
    
    /**
     * Account is active and can perform transactions
     */
    ACTIVE,
    
    /**
     * Account is temporarily suspended
     */
    SUSPENDED,
    
    /**
     * Account is frozen due to security or compliance issues
     */
    FROZEN,
    
    /**
     * Account is permanently closed
     */
    CLOSED,
    
    /**
     * Account is in the process of being closed
     */
    CLOSING,
    
    /**
     * Account is dormant due to inactivity
     */
    DORMANT,
    
    /**
     * Account is blocked due to security violations
     */
    BLOCKED
} 