package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.models.enums.LoanType;

/**
 * Service for generating unique loan numbers
 */
public interface LoanNumberService {
    
    /**
     * Generate a unique loan number for the given loan type
     */
    String generateLoanNumber(LoanType loanType);
}
