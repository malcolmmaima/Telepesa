package com.maelcolium.telepesa.bill.payment.provider;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;

import java.math.BigDecimal;

/**
 * Interface for bill payment providers (KPLC, Water companies, etc.)
 */
public interface PaymentProvider {
    
    /**
     * Checks if this provider supports the given bill type and service provider
     */
    boolean supports(BillPayment.BillType billType, String serviceProvider);
    
    /**
     * Validates bill details and retrieves bill information
     */
    BillValidationResult validateBill(BillValidationRequest request);
    
    /**
     * Processes the bill payment
     */
    PaymentResult processPayment(PaymentRequest request);
    
    /**
     * Gets the service fee for a payment amount
     */
    BigDecimal getServiceFee(BigDecimal amount, BillPayment.BillType billType);
    
    /**
     * Checks payment status from provider
     */
    PaymentStatus checkPaymentStatus(String providerTransactionId);
    
    /**
     * Provider name for identification
     */
    String getProviderName();
    
    // DTOs for provider communication
    record BillValidationRequest(
        String billNumber,
        String serviceProvider,
        BillPayment.BillType billType,
        String customerName,
        String meterNumber,
        String accountNumber
    ) {}
    
    record BillValidationResult(
        boolean valid,
        String customerName,
        BigDecimal outstandingAmount,
        String billDescription,
        String errorMessage
    ) {}
    
    record PaymentRequest(
        String billNumber,
        String customerName,
        BigDecimal amount,
        String serviceProvider,
        BillPayment.BillType billType,
        String paymentReference,
        String meterNumber,
        String accountNumber,
        String phoneNumber
    ) {}
    
    record PaymentResult(
        boolean successful,
        String providerTransactionId,
        String providerReference,
        String errorMessage,
        String receiptNumber
    ) {}
    
    record PaymentStatus(
        String status, // COMPLETED, FAILED, PENDING
        String message,
        String receiptNumber
    ) {}
}
