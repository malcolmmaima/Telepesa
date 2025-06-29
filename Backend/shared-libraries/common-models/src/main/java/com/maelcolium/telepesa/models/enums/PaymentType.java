package com.maelcolium.telepesa.models.enums;

/**
 * Enumeration for different types of payments
 */
public enum PaymentType {
    REGULAR,
    EARLY,
    LATE,
    PARTIAL,
    FULL,
    PENALTY,
    INTEREST_ONLY,
    PRINCIPAL_ONLY,
    BALLOON,
    PREPAYMENT
}
