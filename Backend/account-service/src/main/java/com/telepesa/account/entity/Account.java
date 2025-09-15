package com.telepesa.account.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "minimum_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumBalance;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "is_frozen", nullable = false)
    private Boolean isFrozen;

    @Column(name = "kyc_verified", nullable = false)
    private Boolean kycVerified;

    @Column(name = "overdraft_allowed", nullable = false)
    private Boolean overdraftAllowed;

    @Column(name = "verification_level", nullable = false)
    private Integer verificationLevel;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
