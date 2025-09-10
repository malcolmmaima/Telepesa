package com.maelcolium.telepesa.transfer.dto;

import com.maelcolium.telepesa.transfer.entity.Transfer;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateTransferRequest {
    
    @NotBlank(message = "Recipient account ID is required")
    private String recipientAccountId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000", message = "Amount cannot exceed 1,000,000")
    private BigDecimal amount;
    
    @NotNull(message = "Transfer type is required")
    private Transfer.TransferType transferType;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    @Size(max = 100, message = "Reference cannot exceed 100 characters")
    private String reference;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "KES";
    
    // Recipient details for certain transfer types
    @Size(max = 100, message = "Recipient name cannot exceed 100 characters")
    private String recipientName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String recipientPhoneNumber;
}
