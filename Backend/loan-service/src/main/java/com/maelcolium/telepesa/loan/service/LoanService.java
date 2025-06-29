package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.loan.dto.CreateLoanRequest;
import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for loan operations
 */
public interface LoanService {

    /**
     * Create a new loan application
     */
    LoanDto createLoan(CreateLoanRequest request);

    /**
     * Get loan by ID
     */
    LoanDto getLoan(Long loanId);

    /**
     * Get loan by loan number
     */
    LoanDto getLoanByNumber(String loanNumber);

    /**
     * Get all loans with pagination
     */
    Page<LoanDto> getAllLoans(Pageable pageable);

    /**
     * Get loans by user ID
     */
    Page<LoanDto> getLoansByUserId(Long userId, Pageable pageable);

    /**
     * Get loans by status
     */
    Page<LoanDto> getLoansByStatus(LoanStatus status, Pageable pageable);

    /**
     * Get loans by type
     */
    Page<LoanDto> getLoansByType(LoanType loanType, Pageable pageable);

    /**
     * Get active loans for a user
     */
    List<LoanDto> getActiveLoansByUserId(Long userId);

    /**
     * Approve a loan
     */
    LoanDto approveLoan(Long loanId, Long approvedBy);

    /**
     * Reject a loan
     */
    LoanDto rejectLoan(Long loanId, String rejectionReason);

    /**
     * Disburse a loan
     */
    LoanDto disburseLoan(Long loanId);

    /**
     * Make a payment on a loan
     */
    LoanDto makePayment(Long loanId, BigDecimal amount, String paymentMethod);

    /**
     * Calculate loan payment schedule
     */
    BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal interestRate, Integer termMonths);

    /**
     * Get total outstanding balance for a user
     */
    BigDecimal getTotalOutstandingBalance(Long userId);

    /**
     * Get overdue loans
     */
    List<LoanDto> getOverdueLoans();

    /**
     * Search loans with multiple criteria
     */
    Page<LoanDto> searchLoans(Long userId, LoanStatus status, LoanType loanType, 
                             LocalDate fromDate, LocalDate toDate, Pageable pageable);

    /**
     * Update loan status
     */
    LoanDto updateLoanStatus(Long loanId, LoanStatus status);
}
