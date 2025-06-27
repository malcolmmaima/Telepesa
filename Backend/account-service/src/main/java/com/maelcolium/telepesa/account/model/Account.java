package com.maelcolium.telepesa.account.model;

import com.maelcolium.telepesa.models.BaseEntity;
import com.maelcolium.telepesa.models.enums.AccountStatus;
import com.maelcolium.telepesa.models.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account entity representing a bank account in the Telepesa system.
 * 
 * This entity handles:
 * - Account creation and management
 * - Balance tracking with precision
 * - Account status management
 * - Account types (Savings, Checking, Business, etc.)
 * - User association and ownership
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "accountNumber", unique = true),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_account_status", columnList = "status"),
    @Index(name = "idx_account_type", columnList = "accountType")
})
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Account extends BaseEntity {

    /**
     * Unique account number generated for each account
     */
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Account number is required")
    @Size(max = 20, message = "Account number must not exceed 20 characters")
    private String accountNumber;

    /**
     * User ID who owns this account
     */
    @Column(name = "user_id", nullable = false)
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    /**
     * Type of account (SAVINGS, CHECKING, BUSINESS, FIXED_DEPOSIT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    /**
     * Current account status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "Account status is required")
    @Builder.Default
    private AccountStatus status = AccountStatus.PENDING;

    /**
     * Current account balance with 2 decimal precision
     */
    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Available balance (balance minus holds/pending transactions)
     */
    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Available balance is required")
    @DecimalMin(value = "0.00", message = "Available balance cannot be negative")
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    /**
     * Minimum balance required for this account type
     */
    @Column(name = "minimum_balance", nullable = false, precision = 19, scale = 2)
    @NotNull(message = "Minimum balance is required")
    @DecimalMin(value = "0.00", message = "Minimum balance cannot be negative")
    @Builder.Default
    private BigDecimal minimumBalance = BigDecimal.ZERO;

    /**
     * Daily transaction limit
     */
    @Column(name = "daily_limit", precision = 19, scale = 2)
    @DecimalMin(value = "0.00", message = "Daily limit cannot be negative")
    private BigDecimal dailyLimit;

    /**
     * Monthly transaction limit
     */
    @Column(name = "monthly_limit", precision = 19, scale = 2)
    @DecimalMin(value = "0.00", message = "Monthly limit cannot be negative")
    private BigDecimal monthlyLimit;

    /**
     * Currency code (e.g., KES, USD, EUR)
     */
    @Column(name = "currency_code", nullable = false, length = 3)
    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be exactly 3 characters")
    @Builder.Default
    private String currencyCode = "KES";

    /**
     * Account nickname or alias
     */
    @Column(name = "account_name", length = 100)
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;

    /**
     * Account description
     */
    @Column(name = "description", length = 255)
    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    /**
     * Interest rate for savings accounts (percentage)
     */
    @Column(name = "interest_rate", precision = 5, scale = 4)
    @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
    @DecimalMax(value = "100.0000", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    /**
     * Whether the account is frozen/locked
     */
    @Column(name = "is_frozen", nullable = false)
    @Builder.Default
    private Boolean isFrozen = false;

    /**
     * Whether the account allows overdrafts
     */
    @Column(name = "overdraft_allowed", nullable = false)
    @Builder.Default
    private Boolean overdraftAllowed = false;

    /**
     * Overdraft limit if allowed
     */
    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    @DecimalMin(value = "0.00", message = "Overdraft limit cannot be negative")
    private BigDecimal overdraftLimit;

    /**
     * Last transaction date
     */
    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    /**
     * Account activation date
     */
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    /**
     * Account closure date
     */
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

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

    // Business Methods

    /**
     * Check if account is active and can perform transactions
     */
    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(this.status) && !this.isFrozen;
    }

    /**
     * Check if account can debit specified amount
     */
    public boolean canDebit(BigDecimal amount) {
        if (!isActive()) {
            return false;
        }
        
        BigDecimal newBalance = this.availableBalance.subtract(amount);
        
        if (this.overdraftAllowed && this.overdraftLimit != null) {
            return newBalance.compareTo(this.overdraftLimit.negate()) >= 0;
        }
        
        return newBalance.compareTo(this.minimumBalance) >= 0;
    }

    /**
     * Check if account can credit specified amount
     */
    public boolean canCredit(BigDecimal amount) {
        return isActive() && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Credit account with specified amount
     */
    public void credit(BigDecimal amount) {
        if (!canCredit(amount)) {
            throw new IllegalArgumentException("Cannot credit account: " + accountNumber);
        }
        
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }

    /**
     * Debit account with specified amount
     */
    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalArgumentException("Cannot debit account: " + accountNumber);
        }
        
        this.balance = this.balance.subtract(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
        this.lastTransactionDate = LocalDateTime.now();
    }

    /**
     * Activate the account
     */
    public void activate() {
        this.status = AccountStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    /**
     * Close the account
     */
    public void close() {
        this.status = AccountStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    /**
     * Freeze the account
     */
    public void freeze() {
        this.isFrozen = true;
    }

    /**
     * Unfreeze the account
     */
    public void unfreeze() {
        this.isFrozen = false;
    }

    /**
     * Updates KYC verification status
     */
    public void updateKycStatus(boolean verified, int verificationLevel) {
        this.kycVerified = verified;
        this.verificationLevel = verificationLevel;
    }
} 