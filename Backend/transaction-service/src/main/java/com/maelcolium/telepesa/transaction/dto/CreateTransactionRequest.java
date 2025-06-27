package com.maelcolium.telepesa.transaction.dto;

import com.maelcolium.telepesa.models.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

    @NotNull(message = "From account ID is required")
    private Long fromAccountId;

    private Long toAccountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;
} 