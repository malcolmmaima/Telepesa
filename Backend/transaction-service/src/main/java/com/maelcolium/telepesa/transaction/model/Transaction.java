package com.maelcolium.telepesa.transaction.model;

import com.maelcolium.telepesa.models.BaseEntity;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends BaseEntity {

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id")
    private Long toAccountId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "reference_number", unique = true)
    private String referenceNumber;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fee_amount", precision = 19, scale = 2)
    private BigDecimal feeAmount;

    @Column(name = "total_amount", precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }
} 