package com.maelcolium.telepesa.models.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long id;
    private Long userId;
    private Long accountId;
    private Long loanProductId;
    private String loanNumber;
    private LoanType loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer termInMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal totalAmount;
    private BigDecimal outstandingBalance;
    private BigDecimal totalPaid;
    private LoanStatus status;
    private String purpose;
    private String collateralDescription;
    private BigDecimal collateralValue;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime applicationDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime approvalDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime disbursementDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime maturityDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime nextPaymentDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields for display
    private String userName;
    private String accountNumber;
    private String loanProductName;
    private Integer paymentsRemaining;
    private Boolean isOverdue;
    private Integer daysPastDue;
}
