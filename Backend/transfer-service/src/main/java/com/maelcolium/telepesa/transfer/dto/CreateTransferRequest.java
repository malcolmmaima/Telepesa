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
    
    // SWIFT specific fields
    @Size(max = 11, message = "SWIFT code cannot exceed 11 characters")
    private String swiftCode;
    
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String recipientBankName;
    
    @Size(max = 200, message = "Bank address cannot exceed 200 characters")
    private String recipientBankAddress;
    
    @Size(max = 11, message = "Intermediary bank SWIFT cannot exceed 11 characters")
    private String intermediaryBankSwift;
    
    // RTGS specific fields
    @Size(max = 6, message = "Sort code must be 6 characters")
    private String sortCode;
    
    // PesaLink specific fields
    @Size(max = 3, message = "Bank code cannot exceed 3 characters")
    private String pesalinkBankCode;
    
    // M-Pesa specific fields
    @Pattern(regexp = "^254[0-9]{9}$", message = "M-Pesa number must be in format 254XXXXXXXXX")
    private String mpesaNumber;
}
