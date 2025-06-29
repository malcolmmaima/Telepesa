package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CreateLoanRequest Tests")
class CreateLoanRequestTest {

    @Test
    @DisplayName("Should create CreateLoanRequest with builder")
    void shouldCreateCreateLoanRequestWithBuilder() {
        // When
        CreateLoanRequest request = CreateLoanRequest.builder()
                .userId(100L)
                .accountNumber("ACC123456789")
                .loanType(LoanType.PERSONAL)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Loan application for home improvement")
                .build();

        // Then
        assertThat(request.getUserId()).isEqualTo(100L);
        assertThat(request.getAccountNumber()).isEqualTo("ACC123456789");
        assertThat(request.getLoanType()).isEqualTo(LoanType.PERSONAL);
        assertThat(request.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(request.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.12"));
        assertThat(request.getTermMonths()).isEqualTo(12);
        assertThat(request.getMonthlyIncome()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(request.getPurpose()).isEqualTo("Home improvement");
        assertThat(request.getNotes()).isEqualTo("Loan application for home improvement");
    }

    @Test
    @DisplayName("Should create CreateLoanRequest with no-args constructor and setters")
    void shouldCreateCreateLoanRequestWithNoArgsConstructorAndSetters() {
        // Given
        CreateLoanRequest request = new CreateLoanRequest();

        // When
        request.setUserId(100L);
        request.setAccountNumber("ACC123456789");
        request.setLoanType(LoanType.PERSONAL);
        request.setPrincipalAmount(new BigDecimal("50000.00"));
        request.setInterestRate(new BigDecimal("0.12"));
        request.setTermMonths(12);
        request.setMonthlyIncome(new BigDecimal("8000.00"));
        request.setPurpose("Home improvement");
        request.setNotes("Loan application for home improvement");

        // Then
        assertThat(request.getUserId()).isEqualTo(100L);
        assertThat(request.getAccountNumber()).isEqualTo("ACC123456789");
        assertThat(request.getLoanType()).isEqualTo(LoanType.PERSONAL);
        assertThat(request.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(request.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.12"));
        assertThat(request.getTermMonths()).isEqualTo(12);
        assertThat(request.getMonthlyIncome()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(request.getPurpose()).isEqualTo("Home improvement");
        assertThat(request.getNotes()).isEqualTo("Loan application for home improvement");
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        CreateLoanRequest request1 = CreateLoanRequest.builder()
                .userId(100L)
                .accountNumber("ACC123456789")
                .loanType(LoanType.PERSONAL)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Loan application for home improvement")
                .build();

        CreateLoanRequest request2 = CreateLoanRequest.builder()
                .userId(100L)
                .accountNumber("ACC123456789")
                .loanType(LoanType.PERSONAL)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Loan application for home improvement")
                .build();

        CreateLoanRequest request3 = CreateLoanRequest.builder()
                .userId(101L)
                .accountNumber("ACC123456790")
                .loanType(LoanType.BUSINESS)
                .principalAmount(new BigDecimal("100000.00"))
                .interestRate(new BigDecimal("0.15"))
                .termMonths(24)
                .monthlyIncome(new BigDecimal("15000.00"))
                .purpose("Business expansion")
                .notes("Business loan application")
                .build();

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1).isNotEqualTo(null);
        assertThat(request1).isNotEqualTo("string");

        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
        assertThat(request1.hashCode()).isNotEqualTo(request3.hashCode());
    }

    @Test
    @DisplayName("Should test edge cases with null values")
    void shouldTestEdgeCasesWithNullValues() {
        // Given
        CreateLoanRequest request = new CreateLoanRequest();

        // When & Then
        assertThat(request.getUserId()).isNull();
        assertThat(request.getAccountNumber()).isNull();
        assertThat(request.getLoanType()).isNull();
        assertThat(request.getPrincipalAmount()).isNull();
        assertThat(request.getInterestRate()).isNull();
        assertThat(request.getTermMonths()).isNull();
        assertThat(request.getMonthlyIncome()).isNull();
        assertThat(request.getPurpose()).isNull();
        assertThat(request.getNotes()).isNull();
    }

    @Test
    @DisplayName("Should test all loan types")
    void shouldTestAllLoanTypes() {
        // Given & When & Then
        for (LoanType loanType : LoanType.values()) {
            CreateLoanRequest request = CreateLoanRequest.builder()
                    .loanType(loanType)
                    .build();
            
            assertThat(request.getLoanType()).isEqualTo(loanType);
        }
    }
} 