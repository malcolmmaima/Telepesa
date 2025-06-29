package com.maelcolium.telepesa.loan.dto;

import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoanDto Tests")
class LoanDtoTest {

    @Test
    @DisplayName("Should create LoanDto with builder")
    void shouldCreateLoanDtoWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate applicationDate = LocalDate.now();
        LocalDate approvalDate = LocalDate.now().plusDays(1);
        LocalDate disbursementDate = LocalDate.now().plusDays(2);
        LocalDate nextPaymentDate = LocalDate.now().plusDays(30);

        // When
        LoanDto loanDto = LoanDto.builder()
                .id(1L)
                .loanNumber("PL202412001234")
                .accountNumber("ACC123456789")
                .userId(100L)
                .loanType(LoanType.PERSONAL)
                .status(LoanStatus.ACTIVE)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyPayment(new BigDecimal("4500.00"))
                .outstandingBalance(new BigDecimal("45000.00"))
                .totalPaid(new BigDecimal("5000.00"))
                .applicationDate(applicationDate)
                .approvalDate(approvalDate)
                .disbursementDate(disbursementDate)
                .nextPaymentDate(nextPaymentDate)
                .approvedBy(200L)
                .creditScore(750)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Approved loan for home improvement")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(loanDto.getId()).isEqualTo(1L);
        assertThat(loanDto.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(loanDto.getAccountNumber()).isEqualTo("ACC123456789");
        assertThat(loanDto.getUserId()).isEqualTo(100L);
        assertThat(loanDto.getLoanType()).isEqualTo(LoanType.PERSONAL);
        assertThat(loanDto.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loanDto.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(loanDto.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.12"));
        assertThat(loanDto.getTermMonths()).isEqualTo(12);
        assertThat(loanDto.getMonthlyPayment()).isEqualByComparingTo(new BigDecimal("4500.00"));
        assertThat(loanDto.getOutstandingBalance()).isEqualByComparingTo(new BigDecimal("45000.00"));
        assertThat(loanDto.getTotalPaid()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(loanDto.getApplicationDate()).isEqualTo(applicationDate);
        assertThat(loanDto.getApprovalDate()).isEqualTo(approvalDate);
        assertThat(loanDto.getDisbursementDate()).isEqualTo(disbursementDate);
        assertThat(loanDto.getNextPaymentDate()).isEqualTo(nextPaymentDate);
        assertThat(loanDto.getApprovedBy()).isEqualTo(200L);
        assertThat(loanDto.getCreditScore()).isEqualTo(750);
        assertThat(loanDto.getMonthlyIncome()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(loanDto.getPurpose()).isEqualTo("Home improvement");
        assertThat(loanDto.getNotes()).isEqualTo("Approved loan for home improvement");
        assertThat(loanDto.getCreatedAt()).isEqualTo(now);
        assertThat(loanDto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create LoanDto with no-args constructor and setters")
    void shouldCreateLoanDtoWithNoArgsConstructorAndSetters() {
        // Given
        LoanDto loanDto = new LoanDto();
        LocalDateTime now = LocalDateTime.now();
        LocalDate applicationDate = LocalDate.now();

        // When
        loanDto.setId(1L);
        loanDto.setLoanNumber("PL202412001234");
        loanDto.setAccountNumber("ACC123456789");
        loanDto.setUserId(100L);
        loanDto.setLoanType(LoanType.PERSONAL);
        loanDto.setStatus(LoanStatus.ACTIVE);
        loanDto.setPrincipalAmount(new BigDecimal("50000.00"));
        loanDto.setInterestRate(new BigDecimal("0.12"));
        loanDto.setTermMonths(12);
        loanDto.setMonthlyPayment(new BigDecimal("4500.00"));
        loanDto.setOutstandingBalance(new BigDecimal("45000.00"));
        loanDto.setTotalPaid(new BigDecimal("5000.00"));
        loanDto.setApplicationDate(applicationDate);
        loanDto.setApprovalDate(applicationDate.plusDays(1));
        loanDto.setDisbursementDate(applicationDate.plusDays(2));
        loanDto.setNextPaymentDate(applicationDate.plusDays(30));
        loanDto.setApprovedBy(200L);
        loanDto.setCreditScore(750);
        loanDto.setMonthlyIncome(new BigDecimal("8000.00"));
        loanDto.setPurpose("Home improvement");
        loanDto.setNotes("Approved loan for home improvement");
        loanDto.setCreatedAt(now);
        loanDto.setUpdatedAt(now);

        // Then
        assertThat(loanDto.getId()).isEqualTo(1L);
        assertThat(loanDto.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(loanDto.getAccountNumber()).isEqualTo("ACC123456789");
        assertThat(loanDto.getUserId()).isEqualTo(100L);
        assertThat(loanDto.getLoanType()).isEqualTo(LoanType.PERSONAL);
        assertThat(loanDto.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loanDto.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(loanDto.getInterestRate()).isEqualByComparingTo(new BigDecimal("0.12"));
        assertThat(loanDto.getTermMonths()).isEqualTo(12);
        assertThat(loanDto.getMonthlyPayment()).isEqualByComparingTo(new BigDecimal("4500.00"));
        assertThat(loanDto.getOutstandingBalance()).isEqualByComparingTo(new BigDecimal("45000.00"));
        assertThat(loanDto.getTotalPaid()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(loanDto.getApplicationDate()).isEqualTo(applicationDate);
        assertThat(loanDto.getApprovalDate()).isEqualTo(applicationDate.plusDays(1));
        assertThat(loanDto.getDisbursementDate()).isEqualTo(applicationDate.plusDays(2));
        assertThat(loanDto.getNextPaymentDate()).isEqualTo(applicationDate.plusDays(30));
        assertThat(loanDto.getApprovedBy()).isEqualTo(200L);
        assertThat(loanDto.getCreditScore()).isEqualTo(750);
        assertThat(loanDto.getMonthlyIncome()).isEqualByComparingTo(new BigDecimal("8000.00"));
        assertThat(loanDto.getPurpose()).isEqualTo("Home improvement");
        assertThat(loanDto.getNotes()).isEqualTo("Approved loan for home improvement");
        assertThat(loanDto.getCreatedAt()).isEqualTo(now);
        assertThat(loanDto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void shouldTestEqualsAndHashCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDate applicationDate = LocalDate.now();

        LoanDto loanDto1 = LoanDto.builder()
                .id(1L)
                .loanNumber("PL202412001234")
                .accountNumber("ACC123456789")
                .userId(100L)
                .loanType(LoanType.PERSONAL)
                .status(LoanStatus.ACTIVE)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyPayment(new BigDecimal("4500.00"))
                .outstandingBalance(new BigDecimal("45000.00"))
                .totalPaid(new BigDecimal("5000.00"))
                .applicationDate(applicationDate)
                .approvalDate(applicationDate.plusDays(1))
                .disbursementDate(applicationDate.plusDays(2))
                .nextPaymentDate(applicationDate.plusDays(30))
                .approvedBy(200L)
                .creditScore(750)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Approved loan for home improvement")
                .createdAt(now)
                .updatedAt(now)
                .build();

        LoanDto loanDto2 = LoanDto.builder()
                .id(1L)
                .loanNumber("PL202412001234")
                .accountNumber("ACC123456789")
                .userId(100L)
                .loanType(LoanType.PERSONAL)
                .status(LoanStatus.ACTIVE)
                .principalAmount(new BigDecimal("50000.00"))
                .interestRate(new BigDecimal("0.12"))
                .termMonths(12)
                .monthlyPayment(new BigDecimal("4500.00"))
                .outstandingBalance(new BigDecimal("45000.00"))
                .totalPaid(new BigDecimal("5000.00"))
                .applicationDate(applicationDate)
                .approvalDate(applicationDate.plusDays(1))
                .disbursementDate(applicationDate.plusDays(2))
                .nextPaymentDate(applicationDate.plusDays(30))
                .approvedBy(200L)
                .creditScore(750)
                .monthlyIncome(new BigDecimal("8000.00"))
                .purpose("Home improvement")
                .notes("Approved loan for home improvement")
                .createdAt(now)
                .updatedAt(now)
                .build();

        LoanDto loanDto3 = LoanDto.builder()
                .id(2L)
                .loanNumber("PL202412001235")
                .accountNumber("ACC123456790")
                .userId(101L)
                .loanType(LoanType.BUSINESS)
                .status(LoanStatus.PENDING)
                .principalAmount(new BigDecimal("100000.00"))
                .interestRate(new BigDecimal("0.15"))
                .termMonths(24)
                .monthlyPayment(new BigDecimal("5000.00"))
                .outstandingBalance(new BigDecimal("100000.00"))
                .totalPaid(new BigDecimal("0.00"))
                .applicationDate(applicationDate)
                .approvalDate(null)
                .disbursementDate(null)
                .nextPaymentDate(null)
                .approvedBy(null)
                .creditScore(700)
                .monthlyIncome(new BigDecimal("15000.00"))
                .purpose("Business expansion")
                .notes("Pending business loan application")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Then
        assertThat(loanDto1).isEqualTo(loanDto2);
        assertThat(loanDto1).isNotEqualTo(loanDto3);
        assertThat(loanDto1).isNotEqualTo(null);
        assertThat(loanDto1).isNotEqualTo("string");

        assertThat(loanDto1.hashCode()).isEqualTo(loanDto2.hashCode());
        assertThat(loanDto1.hashCode()).isNotEqualTo(loanDto3.hashCode());
    }

    @Test
    @DisplayName("Should test edge cases with null values")
    void shouldTestEdgeCasesWithNullValues() {
        // Given
        LoanDto loanDto = new LoanDto();

        // When & Then
        assertThat(loanDto.getId()).isNull();
        assertThat(loanDto.getLoanNumber()).isNull();
        assertThat(loanDto.getAccountNumber()).isNull();
        assertThat(loanDto.getUserId()).isNull();
        assertThat(loanDto.getLoanType()).isNull();
        assertThat(loanDto.getStatus()).isNull();
        assertThat(loanDto.getPrincipalAmount()).isNull();
        assertThat(loanDto.getInterestRate()).isNull();
        assertThat(loanDto.getTermMonths()).isNull();
        assertThat(loanDto.getMonthlyPayment()).isNull();
        assertThat(loanDto.getOutstandingBalance()).isNull();
        assertThat(loanDto.getTotalPaid()).isNull();
        assertThat(loanDto.getApplicationDate()).isNull();
        assertThat(loanDto.getApprovalDate()).isNull();
        assertThat(loanDto.getDisbursementDate()).isNull();
        assertThat(loanDto.getNextPaymentDate()).isNull();
        assertThat(loanDto.getApprovedBy()).isNull();
        assertThat(loanDto.getCreditScore()).isNull();
        assertThat(loanDto.getMonthlyIncome()).isNull();
        assertThat(loanDto.getPurpose()).isNull();
        assertThat(loanDto.getNotes()).isNull();
        assertThat(loanDto.getCreatedAt()).isNull();
        assertThat(loanDto.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("Should test all loan types")
    void shouldTestAllLoanTypes() {
        // Given & When & Then
        for (LoanType loanType : LoanType.values()) {
            LoanDto loanDto = LoanDto.builder()
                    .loanType(loanType)
                    .build();
            
            assertThat(loanDto.getLoanType()).isEqualTo(loanType);
        }
    }

    @Test
    @DisplayName("Should test all loan statuses")
    void shouldTestAllLoanStatuses() {
        // Given & When & Then
        for (LoanStatus status : LoanStatus.values()) {
            LoanDto loanDto = LoanDto.builder()
                    .status(status)
                    .build();
            
            assertThat(loanDto.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Should test toBuilder functionality")
    void shouldTestToBuilderFunctionality() {
        // Given
        LoanDto original = LoanDto.builder()
                .id(1L)
                .loanNumber("PL202412001234")
                .loanType(LoanType.PERSONAL)
                .status(LoanStatus.ACTIVE)
                .principalAmount(new BigDecimal("50000.00"))
                .build();

        // When
        LoanDto modified = original.toBuilder()
                .id(2L)
                .loanNumber("PL202412001235")
                .loanType(LoanType.BUSINESS)
                .status(LoanStatus.PENDING)
                .principalAmount(new BigDecimal("100000.00"))
                .build();

        // Then
        assertThat(modified.getId()).isEqualTo(2L);
        assertThat(modified.getLoanNumber()).isEqualTo("PL202412001235");
        assertThat(modified.getLoanType()).isEqualTo(LoanType.BUSINESS);
        assertThat(modified.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(modified.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        
        // Original should remain unchanged
        assertThat(original.getId()).isEqualTo(1L);
        assertThat(original.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(original.getLoanType()).isEqualTo(LoanType.PERSONAL);
        assertThat(original.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(original.getPrincipalAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
    }
} 