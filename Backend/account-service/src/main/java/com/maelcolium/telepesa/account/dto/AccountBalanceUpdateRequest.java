package com.maelcolium.telepesa.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for account balance update operations (credit/debit)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Account balance update request")
public class AccountBalanceUpdateRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to credit/debit", example = "1000.00")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description of the transaction", example = "Deposit")
    private String description;
} 