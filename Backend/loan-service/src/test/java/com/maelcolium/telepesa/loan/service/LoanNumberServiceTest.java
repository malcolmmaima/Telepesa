package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.loan.repository.LoanRepository;
import com.maelcolium.telepesa.loan.service.impl.LoanNumberServiceImpl;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoanNumberService
 */
@ExtendWith(MockitoExtension.class)
class LoanNumberServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanNumberServiceImpl loanNumberService;

    @BeforeEach
    void setUp() {
        // Setup any common test data
    }

    @Test
    void generateLoanNumber_ForPersonalLoan_ShouldGenerateWithPLPrefix() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.PERSONAL);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("PL");
        assertThat(loanNumber).hasSize(14); // PL + 8 digits (date) + 4 digits (random)
    }

    @Test
    void generateLoanNumber_ForBusinessLoan_ShouldGenerateWithBLPrefix() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.BUSINESS);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("BL");
        assertThat(loanNumber).hasSize(14);
    }

    @Test
    void generateLoanNumber_ForMortgageLoan_ShouldGenerateWithMLPrefix() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.MORTGAGE);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("ML");
        assertThat(loanNumber).hasSize(14);
    }

    @Test
    void generateLoanNumber_ForAutoLoan_ShouldGenerateWithALPrefix() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.AUTO);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("AL");
        assertThat(loanNumber).hasSize(14);
    }

    @Test
    void generateLoanNumber_ForEducationLoan_ShouldGenerateWithELPrefix() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.EDUCATION);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("EL");
        assertThat(loanNumber).hasSize(14);
    }

    @Test
    void generateLoanNumber_WithDuplicateNumbers_ShouldRetryAndGenerateUnique() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString()))
            .thenReturn(true)  // First attempt - duplicate
            .thenReturn(true)  // Second attempt - duplicate
            .thenReturn(false); // Third attempt - unique

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.PERSONAL);

        // Then
        assertThat(loanNumber).isNotNull();
        assertThat(loanNumber).startsWith("PL");
    }

    @Test
    void generateLoanNumber_WithMaxAttemptsReached_ShouldThrowException() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(true);

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> loanNumberService.generateLoanNumber(LoanType.PERSONAL));
    }

    @Test
    void generateLoanNumber_ShouldGenerateUniqueNumbers() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber1 = loanNumberService.generateLoanNumber(LoanType.PERSONAL);
        String loanNumber2 = loanNumberService.generateLoanNumber(LoanType.PERSONAL);

        // Then
        assertThat(loanNumber1).isNotNull();
        assertThat(loanNumber2).isNotNull();
        assertThat(loanNumber1).isNotEqualTo(loanNumber2);
    }

    @Test
    void generateLoanNumber_ShouldIncludeDateInNumber() {
        // Given
        when(loanRepository.existsByLoanNumber(anyString())).thenReturn(false);

        // When
        String loanNumber = loanNumberService.generateLoanNumber(LoanType.PERSONAL);

        // Then
        assertThat(loanNumber).isNotNull();
        // Extract date part (characters 2-10: YYYYMMDD)
        String datePart = loanNumber.substring(2, 10);
        assertThat(datePart).matches("\\d{8}"); // Should be 8 digits
    }
}
