package com.maelcolium.telepesa.bill.payment.dto;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BillPaymentResponse {
    
    private String id;
    private String paymentReference;
    private String accountId;
    private String billNumber;
    private String customerName;
    private BillPayment.BillType billType;
    private String serviceProvider;
    private BigDecimal amount;
    private String currency;
    private BigDecimal serviceFee;
    private BigDecimal totalAmount;
    private BillPayment.PaymentStatus status;
    private String description;
    
    // Payment processing info
    private String providerTransactionId;
    private String providerReference;
    private LocalDateTime processedAt;
    private String failureReason;
    
    // Bill details
    private LocalDateTime dueDate;
    private String meterNumber;
    private String accountNumber;
    private String phoneNumber;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
