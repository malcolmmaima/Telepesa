package com.maelcolium.telepesa.bill.payment.provider.impl;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import com.maelcolium.telepesa.bill.payment.provider.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * KPLC (Kenya Power) Payment Provider
 * Handles electricity bill payments
 */
@Service
@Slf4j
public class KPLCPaymentProvider implements PaymentProvider {
    
    @Override
    public boolean supports(BillPayment.BillType billType, String serviceProvider) {
        return billType == BillPayment.BillType.ELECTRICITY && 
               ("KPLC".equalsIgnoreCase(serviceProvider) || 
                "Kenya Power".equalsIgnoreCase(serviceProvider));
    }
    
    @Override
    public BillValidationResult validateBill(BillValidationRequest request) {
        log.info("Validating KPLC bill for account: {}", request.billNumber());
        
        // Mock validation - In real implementation, call KPLC API
        try {
            // Simulate API call delay
            Thread.sleep(500);
            
            // Mock validation logic
            if (request.billNumber() == null || request.billNumber().length() < 6) {
                return new BillValidationResult(
                    false, null, null, null, 
                    "Invalid account number format"
                );
            }
            
            // Mock successful validation with outstanding amount
            String customerName = request.customerName() != null ? 
                request.customerName() : "JOHN DOE CUSTOMER";
            
            // Generate mock outstanding amount based on account number
            BigDecimal outstandingAmount = generateMockAmount(request.billNumber());
            
            return new BillValidationResult(
                true, 
                customerName, 
                outstandingAmount, 
                "Electricity bill payment for " + request.meterNumber(),
                null
            );
            
        } catch (Exception e) {
            log.error("Error validating KPLC bill: {}", e.getMessage());
            return new BillValidationResult(
                false, null, null, null, 
                "Service temporarily unavailable"
            );
        }
    }
    
    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("Processing KPLC payment for bill: {}, amount: {}", 
                request.billNumber(), request.amount());
        
        try {
            // Simulate payment processing delay
            Thread.sleep(1000);
            
            // Mock payment processing - In real implementation, call KPLC payment API
            String providerTransactionId = "KPLC" + System.currentTimeMillis();
            String receiptNumber = "RCP" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // Simulate 95% success rate
            boolean successful = Math.random() > 0.05;
            
            if (successful) {
                return new PaymentResult(
                    true,
                    providerTransactionId,
                    "KPLC-" + request.paymentReference(),
                    null,
                    receiptNumber
                );
            } else {
                return new PaymentResult(
                    false,
                    null,
                    null,
                    "Payment failed at KPLC gateway",
                    null
                );
            }
            
        } catch (Exception e) {
            log.error("Error processing KPLC payment: {}", e.getMessage());
            return new PaymentResult(
                false, null, null, 
                "Service temporarily unavailable", 
                null
            );
        }
    }
    
    @Override
    public BigDecimal getServiceFee(BigDecimal amount, BillPayment.BillType billType) {
        // KPLC charges a flat fee of KES 30 for payments above 500, otherwise KES 20
        if (amount.compareTo(new BigDecimal("500")) >= 0) {
            return new BigDecimal("30.00");
        } else {
            return new BigDecimal("20.00");
        }
    }
    
    @Override
    public PaymentStatus checkPaymentStatus(String providerTransactionId) {
        log.info("Checking KPLC payment status for transaction: {}", providerTransactionId);
        
        try {
            // Simulate status check delay
            Thread.sleep(300);
            
            // Mock status check - In real implementation, call KPLC status API
            return new PaymentStatus(
                "COMPLETED",
                "Payment successful",
                "RCP" + providerTransactionId.substring(4, 12)
            );
            
        } catch (Exception e) {
            log.error("Error checking KPLC payment status: {}", e.getMessage());
            return new PaymentStatus(
                "UNKNOWN",
                "Status check failed",
                null
            );
        }
    }
    
    @Override
    public String getProviderName() {
        return "KPLC";
    }
    
    /**
     * Generate mock outstanding amount based on account number
     */
    private BigDecimal generateMockAmount(String accountNumber) {
        // Generate deterministic amount based on account number hash
        int hash = Math.abs(accountNumber.hashCode());
        double amount = 500 + (hash % 4500); // Between 500 and 5000
        return new BigDecimal(amount).setScale(2, RoundingMode.HALF_UP);
    }
}
