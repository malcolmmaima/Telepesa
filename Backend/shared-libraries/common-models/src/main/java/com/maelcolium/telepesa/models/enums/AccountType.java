package com.maelcolium.telepesa.models.enums;

/**
 * Enumeration of different types of banking accounts.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
public enum AccountType {
    
    /**
     * Personal savings account
     */
    SAVINGS,
    
    /**
     * Checking/Current account for regular transactions
     */
    CHECKING,
    
    /**
     * Business account for commercial entities
     */
    BUSINESS,
    
    /**
     * Joint account shared between multiple users
     */
    JOINT,
    
    /**
     * Fixed deposit account
     */
    FIXED_DEPOSIT,
    
    /**
     * Money market account
     */
    MONEY_MARKET,
    
    /**
     * Student account with special benefits
     */
    STUDENT,
    
    /**
     * Premium account with additional benefits
     */
    PREMIUM,
    
    /**
     * Corporate account for large businesses
     */
    CORPORATE,
    
    /**
     * Trust account managed on behalf of others
     */
    TRUST,
    
    /**
     * Escrow account for holding funds temporarily
     */
    ESCROW
} 