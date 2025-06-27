package com.maelcolium.telepesa.account.model;

import com.maelcolium.telepesa.models.BaseEntity;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Banking Account entity representing customer accounts in the Telepesa platform.
 * 
 * This entity handles:
 * - Account identification and ownership
 * - Balance management and tracking
 * - Account status and type management
 * - Audit trail and compliance requirements
 */
@Entity
@Table(name = "accounts", 
       indexes = {
           @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
           @Index(name = "idx_user_id", columnList = "userId"),
           @Index(name = "idx_account_status", columnList = "status"),
           @Index(name = "idx_account_type", columnList = "accountType"),
           @Index(name = "idx_created_at", columnList = "createdAt")
       })
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"transactions"})
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique account number generated for this account
     */
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be between 10 and 20 characters")
    private String accountNumber;

    /**
     * User ID that owns this account (references user-service)
     */
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Type of account (SAVINGS, CHECKING, BUSINESS, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    /**
     * Current account status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @NotNull(message = "Account status is required")
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING;

    /**
     * Current account balance
     */
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Available balance (balance minus holds/pending transactions)
     */
    @Column(name = "available_balance", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Available balance is required")
    @DecimalMin(value = "0.00", message = "Available balance cannot be negative")
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /**
     * Minimum balance required for this account
     */
    @Column(name = "minimum_balance", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Minimum balance is required")
    @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
    @Builder.Default
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    /**
     * Daily transaction limit for this account
     */
    @Column(name = "daily_limit", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Daily limit cannot be negative")
    private BigDecimal dailyLimit;

    /**
     * Monthly transaction limit for this account
     */
    @Column(name = "monthly_limit", precision = 15, scale = 2)
    @DecimalMin(value = "0.00", message = "Monthly limit cannot be negative")
    private BigDecimal monthlyLimit;

    /**
     * Currency code for this account (KES, USD, etc.)
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    private String currencyCode = "KES";

    /**
     * Interest rate for savings accounts (if applicable)
     */
    @Column(name = "interest_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
    @DecimalMax(value = "1.0000", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    /**
     * Account opening date
     */
    @Column(name = "opened_at", nullable = false)
    @NotNull(message = "Account opening date is required")
    @Builder.Default
    private LocalDateTime openedAt = LocalDateTime.now();

    /**
     * Account closure date (if closed)
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    /**
     * Last transaction date for activity tracking
     */
    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    /**
     * KYC verification status
     */
    @Column(name = "kyc_verified", nullable = false)
    @Builder.Default
    private Boolean kycVerified = false;

    /**
     * Account verification level (0-3, higher = more verified)
     */
    @Column(name = "verification_level", nullable = false)
    @Min(value = 0, message = "Verification level must be at least 0")
    @Max(value = 3, message = "Verification level cannot exceed 3")
    @Builder.Default
    private Integer verificationLevel = 0;

    /**
     * Account notes or remarks
     */
    @Column(name = "notes", length = 1000)
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;

    /**
     * Account creation timestamp
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Last modification timestamp
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Business Methods

    /**
     * Checks if the account is active and can perform transactions
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status);
    }

    /**
     * Checks if the account is frozen
     */
    public boolean isFrozen() {
        return AccountStatus.FROZEN.equals(this.status);
    }

    /**
     * Checks if the account is closed
     */
    public boolean isClosed() {
        return AccountStatus.CLOSED.equals(this.status);
    }

    /**
     * Checks if sufficient balance is available for a transaction
     */
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.availableBalance.compareTo(amount) >= 0;
    }

    /**
     * Checks if the account meets minimum balance requirements
     */
    public boolean meetsMinimumBalance() {
        return this.balance.compareTo(this.minimumBalance) >= 0;
    }

    /**
     * Credits the account with the specified amount
     */
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    /**
     * Debits the account with the specified amount
     */
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    /**
     * Freezes the account
     */
    public void freeze() {
        this.status = AccountStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Activates the account
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Closes the account
     */
    public void close() {
        this.status = AccountStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Updates KYC verification status
     */
    public void updateKycStatus(boolean verified, int verificationLevel) {
        this.kycVerified = verified;
        this.verificationLevel = verificationLevel;
        this.updatedAt = LocalDateTime.now();
    }
} 