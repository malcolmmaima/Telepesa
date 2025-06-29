package com.maelcolium.telepesa.models.enums;

/**
 * Enumeration for payment status states
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED,
    PARTIAL,
    OVERDUE
}
