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
 * DTO for fund transfer operations between accounts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Fund transfer request")
public class TransferFundsRequest {

    @NotBlank(message = "From account number is required")
    @Schema(description = "Source account number", example = "ACC001234567890")
    private String fromAccountNumber;

    @NotBlank(message = "To account number is required")
    @Schema(description = "Destination account number", example = "ACC001234567891")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to transfer", example = "1000.00")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description of the transfer", example = "Transfer to savings account")
    private String description;
} 