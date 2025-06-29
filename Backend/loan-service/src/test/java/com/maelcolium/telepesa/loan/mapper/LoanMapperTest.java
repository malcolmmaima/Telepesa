package com.maelcolium.telepesa.loan.mapper;

import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoanMapper
 */
class LoanMapperTest {

    private LoanMapper loanMapper;
    private Loan testLoan;
    private LoanDto testLoanDto;

    @BeforeEach
    void setUp() {
        loanMapper = Mappers.getMapper(LoanMapper.class);

        testLoan = Loan.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.ACTIVE)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("40000.00"))
            .totalPaid(new BigDecimal("10000.00"))
            .nextPaymentDate(LocalDate.now().plusDays(30))
            .applicationDate(LocalDate.now().minusDays(30))
            .approvalDate(LocalDate.now().minusDays(25))
            .disbursementDate(LocalDate.now().minusDays(20))
            .approvedBy(200L)
            .creditScore(750)
            .purpose("Business expansion")
            .monthlyIncome(new BigDecimal("80000.00"))
            .notes("Good payment history")
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .version(1L)
            .build();

        testLoanDto = LoanDto.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.ACTIVE)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("40000.00"))
            .totalPaid(new BigDecimal("10000.00"))
            .nextPaymentDate(LocalDate.now().plusDays(30))
            .applicationDate(LocalDate.now().minusDays(30))
            .approvalDate(LocalDate.now().minusDays(25))
            .disbursementDate(LocalDate.now().minusDays(20))
            .approvedBy(200L)
            .creditScore(750)
            .purpose("Business expansion")
            .monthlyIncome(new BigDecimal("80000.00"))
            .notes("Good payment history")
            .createdAt(LocalDateTime.now().minusDays(30))
            .updatedAt(LocalDateTime.now().minusDays(1))
            .build();
    }

    @Test
    void toDto_WithValidLoan_ShouldMapAllFields() {
        // When
        LoanDto result = loanMapper.toDto(testLoan);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testLoan.getId());
        assertThat(result.getLoanNumber()).isEqualTo(testLoan.getLoanNumber());
        assertThat(result.getUserId()).isEqualTo(testLoan.getUserId());
        assertThat(result.getAccountNumber()).isEqualTo(testLoan.getAccountNumber());
        assertThat(result.getLoanType()).isEqualTo(testLoan.getLoanType());
        assertThat(result.getStatus()).isEqualTo(testLoan.getStatus());
        assertThat(result.getPrincipalAmount()).isEqualTo(testLoan.getPrincipalAmount());
        assertThat(result.getInterestRate()).isEqualTo(testLoan.getInterestRate());
        assertThat(result.getTermMonths()).isEqualTo(testLoan.getTermMonths());
        assertThat(result.getMonthlyPayment()).isEqualTo(testLoan.getMonthlyPayment());
        assertThat(result.getOutstandingBalance()).isEqualTo(testLoan.getOutstandingBalance());
        assertThat(result.getTotalPaid()).isEqualTo(testLoan.getTotalPaid());
        assertThat(result.getNextPaymentDate()).isEqualTo(testLoan.getNextPaymentDate());
        assertThat(result.getApplicationDate()).isEqualTo(testLoan.getApplicationDate());
        assertThat(result.getApprovalDate()).isEqualTo(testLoan.getApprovalDate());
        assertThat(result.getDisbursementDate()).isEqualTo(testLoan.getDisbursementDate());
        assertThat(result.getApprovedBy()).isEqualTo(testLoan.getApprovedBy());
        assertThat(result.getCreditScore()).isEqualTo(testLoan.getCreditScore());
        assertThat(result.getPurpose()).isEqualTo(testLoan.getPurpose());
        assertThat(result.getMonthlyIncome()).isEqualTo(testLoan.getMonthlyIncome());
        assertThat(result.getNotes()).isEqualTo(testLoan.getNotes());
        assertThat(result.getCreatedAt()).isEqualTo(testLoan.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(testLoan.getUpdatedAt());
    }

    @Test
    void toDto_WithNullLoan_ShouldReturnNull() {
        // When
        LoanDto result = loanMapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toDto_WithMinimalLoan_ShouldMapRequiredFields() {
        // Given
        Loan minimalLoan = Loan.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.PENDING)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("50000.00"))
            .totalPaid(BigDecimal.ZERO)
            .applicationDate(LocalDate.now())
            .build();

        // When
        LoanDto result = loanMapper.toDto(minimalLoan);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(result.getPrincipalAmount()).isEqualTo(new BigDecimal("50000.00"));
    }

    @Test
    void toEntity_WithValidLoanDto_ShouldMapAllFields() {
        // When
        Loan result = loanMapper.toEntity(testLoanDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testLoanDto.getId());
        assertThat(result.getLoanNumber()).isEqualTo(testLoanDto.getLoanNumber());
        assertThat(result.getUserId()).isEqualTo(testLoanDto.getUserId());
        assertThat(result.getAccountNumber()).isEqualTo(testLoanDto.getAccountNumber());
        assertThat(result.getLoanType()).isEqualTo(testLoanDto.getLoanType());
        assertThat(result.getStatus()).isEqualTo(testLoanDto.getStatus());
        assertThat(result.getPrincipalAmount()).isEqualTo(testLoanDto.getPrincipalAmount());
        assertThat(result.getInterestRate()).isEqualTo(testLoanDto.getInterestRate());
        assertThat(result.getTermMonths()).isEqualTo(testLoanDto.getTermMonths());
        assertThat(result.getMonthlyPayment()).isEqualTo(testLoanDto.getMonthlyPayment());
        assertThat(result.getOutstandingBalance()).isEqualTo(testLoanDto.getOutstandingBalance());
        assertThat(result.getTotalPaid()).isEqualTo(testLoanDto.getTotalPaid());
        assertThat(result.getNextPaymentDate()).isEqualTo(testLoanDto.getNextPaymentDate());
        assertThat(result.getApplicationDate()).isEqualTo(testLoanDto.getApplicationDate());
        assertThat(result.getApprovalDate()).isEqualTo(testLoanDto.getApprovalDate());
        assertThat(result.getDisbursementDate()).isEqualTo(testLoanDto.getDisbursementDate());
        assertThat(result.getApprovedBy()).isEqualTo(testLoanDto.getApprovedBy());
        assertThat(result.getCreditScore()).isEqualTo(testLoanDto.getCreditScore());
        assertThat(result.getPurpose()).isEqualTo(testLoanDto.getPurpose());
        assertThat(result.getMonthlyIncome()).isEqualTo(testLoanDto.getMonthlyIncome());
        assertThat(result.getNotes()).isEqualTo(testLoanDto.getNotes());
        assertThat(result.getCreatedAt()).isEqualTo(testLoanDto.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(testLoanDto.getUpdatedAt());
    }

    @Test
    void toEntity_WithNullLoanDto_ShouldReturnNull() {
        // When
        Loan result = loanMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void toEntity_WithMinimalLoanDto_ShouldMapRequiredFields() {
        // Given
        LoanDto minimalLoanDto = LoanDto.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.PENDING)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("50000.00"))
            .totalPaid(BigDecimal.ZERO)
            .applicationDate(LocalDate.now())
            .build();

        // When
        Loan result = loanMapper.toEntity(minimalLoanDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(result.getPrincipalAmount()).isEqualTo(new BigDecimal("50000.00"));
    }

    @Test
    void roundTripMapping_ShouldPreserveData() {
        // When
        LoanDto dto = loanMapper.toDto(testLoan);
        Loan entity = loanMapper.toEntity(dto);

        // Then
        assertThat(entity.getId()).isEqualTo(testLoan.getId());
        assertThat(entity.getLoanNumber()).isEqualTo(testLoan.getLoanNumber());
        assertThat(entity.getUserId()).isEqualTo(testLoan.getUserId());
        assertThat(entity.getAccountNumber()).isEqualTo(testLoan.getAccountNumber());
        assertThat(entity.getLoanType()).isEqualTo(testLoan.getLoanType());
        assertThat(entity.getStatus()).isEqualTo(testLoan.getStatus());
        assertThat(entity.getPrincipalAmount()).isEqualTo(testLoan.getPrincipalAmount());
        assertThat(entity.getInterestRate()).isEqualTo(testLoan.getInterestRate());
        assertThat(entity.getTermMonths()).isEqualTo(testLoan.getTermMonths());
        assertThat(entity.getMonthlyPayment()).isEqualTo(testLoan.getMonthlyPayment());
    }
}
