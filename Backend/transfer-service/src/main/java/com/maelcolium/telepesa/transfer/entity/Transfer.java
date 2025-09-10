package com.maelcolium.telepesa.transfer.entity;

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
@Table(name = "transfers", indexes = {
    @Index(name = "idx_sender_account", columnList = "senderAccountId"),
    @Index(name = "idx_recipient_account", columnList = "recipientAccountId"),
    @Index(name = "idx_transfer_reference", columnList = "transferReference"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String transferReference;
    
    @Column(nullable = false)
    private String senderAccountId;
    
    @Column(nullable = false)
    private String recipientAccountId;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 3)
    private String currency = "KES";
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType transferType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;
    
    @Column(length = 500)
    private String description;
    
    @Column(length = 100)
    private String reference;
    
    // Fees
    @Column(precision = 19, scale = 2)
    private BigDecimal transferFee = BigDecimal.ZERO;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal totalAmount;
    
    // Processing info
    private String processedBy;
    private LocalDateTime processedAt;
    private String failureReason;
    
    // Metadata
    private String senderName;
    private String recipientName;
    private String senderPhoneNumber;
    private String recipientPhoneNumber;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public enum TransferType {
        INTERNAL,      // Within Telepesa accounts
        MOBILE_MONEY,  // To M-Pesa, Airtel Money, etc.
        BANK_TRANSFER, // To bank accounts
        PEER_TO_PEER   // Direct user-to-user
    }
    
    public enum TransferStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
    
    @PrePersist
    protected void onCreate() {
        if (totalAmount == null) {
            totalAmount = amount.add(transferFee);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        if (totalAmount == null) {
            totalAmount = amount.add(transferFee);
        }
    }
}
