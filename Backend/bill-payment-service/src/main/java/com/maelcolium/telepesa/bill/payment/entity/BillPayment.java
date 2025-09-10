package com.maelcolium.telepesa.bill.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_payments", indexes = {
    @Index(name = "idx_account_id", columnList = "accountId"),
    @Index(name = "idx_bill_number", columnList = "billNumber"),
    @Index(name = "idx_payment_reference", columnList = "paymentReference"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_bill_type", columnList = "billType"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String paymentReference;
    
    @Column(nullable = false)
    private String accountId;
    
    @Column(nullable = false)
    private String billNumber;
    
    @Column(nullable = false)
    private String customerName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillType billType;
    
    @Column(nullable = false)
    private String serviceProvider;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 3)
    private String currency = "KES";
    
    @Column(precision = 19, scale = 2)
    private BigDecimal serviceFee = BigDecimal.ZERO;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
    
    @Column(length = 500)
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
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum BillType {
        ELECTRICITY,    // KPLC, Kenya Power
        WATER,         // Nairobi Water, other water companies
        INTERNET,      // Safaricom Home, JTL, etc.
        TV_SUBSCRIPTION, // DSTV, Gotv, Startimes
        MOBILE_POSTPAID, // Safaricom postpaid, Airtel postpaid
        INSURANCE,     // NHIF, private insurance
        SCHOOL_FEES,   // Schools, universities
        GOVERNMENT,    // KRA, government services
        OTHER
    }
    
    public enum PaymentStatus {
        PENDING,
        PROCESSING, 
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
    
    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (totalAmount == null && amount != null && serviceFee != null) {
            totalAmount = amount.add(serviceFee);
        }
    }
}
