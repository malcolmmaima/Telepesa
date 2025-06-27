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
 * DTO for transfer operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Transfer operation request")
public class TransferRequest {

    @NotNull(message = "To account ID is required")
    @Schema(description = "Destination account ID", example = "2")
    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Amount to transfer", example = "500.00")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    @Schema(description = "Description of the transfer", example = "Transfer to checking account")
    private String description;
} 