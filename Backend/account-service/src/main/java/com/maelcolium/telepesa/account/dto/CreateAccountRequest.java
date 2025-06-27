package com.maelcolium.telepesa.account.dto;

import com.maelcolium.telepesa.models.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.00", message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Builder.Default
    private String currencyCode = "KES";

    @DecimalMin(value = "0.00", message = "Daily limit cannot be negative")
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.00", message = "Monthly limit cannot be negative")
    private BigDecimal monthlyLimit;

    @DecimalMin(value = "0.0000", message = "Interest rate cannot be negative")
    @DecimalMax(value = "1.0000", message = "Interest rate cannot exceed 100%")
    private BigDecimal interestRate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
} 