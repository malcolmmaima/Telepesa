package com.maelcolium.telepesa.bill.payment.dto;

import com.maelcolium.telepesa.bill.payment.entity.BillPayment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateBillPaymentRequest {
    
    @NotBlank(message = "Bill number is required")
    @Size(max = 100, message = "Bill number cannot exceed 100 characters")
    private String billNumber;
    
    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name cannot exceed 200 characters")
    private String customerName;
    
    @NotNull(message = "Bill type is required")
    private BillPayment.BillType billType;
    
    @NotBlank(message = "Service provider is required")
    @Size(max = 100, message = "Service provider name cannot exceed 100 characters")
    private String serviceProvider;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1.00")
    @DecimalMax(value = "500000", message = "Amount cannot exceed 500,000")
    private BigDecimal amount;
    
    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currency = "KES";
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    // Bill-specific details
    @Size(max = 50, message = "Meter number cannot exceed 50 characters")
    private String meterNumber;
    
    @Size(max = 50, message = "Account number cannot exceed 50 characters")
    private String accountNumber;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    private LocalDateTime dueDate;
}
