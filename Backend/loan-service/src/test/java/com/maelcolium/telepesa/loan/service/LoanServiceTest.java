package com.maelcolium.telepesa.loan.service;

import com.maelcolium.telepesa.models.dto.LoanDto;
import com.maelcolium.telepesa.models.request.CreateLoanRequest;
import com.maelcolium.telepesa.loan.exception.LoanNotFoundException;
import com.maelcolium.telepesa.loan.exception.LoanOperationException;
import com.maelcolium.telepesa.loan.mapper.LoanMapper;
import com.maelcolium.telepesa.loan.model.Loan;
import com.maelcolium.telepesa.loan.repository.LoanRepository;
import com.maelcolium.telepesa.loan.service.impl.LoanServiceImpl;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LoanService
 */
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanNumberService loanNumberService;

    @InjectMocks
    private LoanServiceImpl loanService;

    private CreateLoanRequest createLoanRequest;
    private Loan testLoan;
    private LoanDto testLoanDto;

    @BeforeEach
    void setUp() {
        createLoanRequest = CreateLoanRequest.builder()
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("12.5000"))
            .termMonths(24)
            .purpose("Business expansion")
            .monthlyIncome(new BigDecimal("80000.00"))
            .notes("Good credit history")
            .build();

        testLoan = Loan.builder()
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

        testLoanDto = LoanDto.builder()
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
    }

    @Test
    void createLoan_WithValidRequest_ShouldReturnLoanDto() {
        // Given
        when(loanNumberService.generateLoanNumber(LoanType.PERSONAL)).thenReturn("PL202412001234");
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        LoanDto result = loanService.createLoan(createLoanRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getPrincipalAmount()).isEqualTo(new BigDecimal("50000.00"));
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PENDING);

        verify(loanNumberService).generateLoanNumber(LoanType.PERSONAL);
        verify(loanRepository).save(any(Loan.class));
        verify(loanMapper).toDto(testLoan);
    }

    @Test
    void getLoan_WithExistingId_ShouldReturnLoanDto() {
        // Given
        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        LoanDto result = loanService.getLoan(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(loanRepository).findById(1L);
        verify(loanMapper).toDto(testLoan);
    }

    @Test
    void getLoan_WithNonExistingId_ShouldThrowException() {
        // Given
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.getLoan(999L));
        verify(loanRepository).findById(999L);
        verify(loanMapper, never()).toDto(any());
    }

    @Test
    void getLoanByNumber_WithExistingNumber_ShouldReturnLoanDto() {
        // Given
        String loanNumber = "PL202412001234";
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.of(testLoan));
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        LoanDto result = loanService.getLoanByNumber(loanNumber);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLoanNumber()).isEqualTo(loanNumber);
        verify(loanRepository).findByLoanNumber(loanNumber);
        verify(loanMapper).toDto(testLoan);
    }

    @Test
    void getLoanByNumber_WithNonExistingNumber_ShouldThrowException() {
        // Given
        String loanNumber = "INVALID123";
        when(loanRepository.findByLoanNumber(loanNumber)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.getLoanByNumber(loanNumber));
        verify(loanRepository).findByLoanNumber(loanNumber);
        verify(loanMapper, never()).toDto(any());
    }

    @Test
    void getAllLoans_ShouldReturnPageOfLoans() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        when(loanRepository.findAll(pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.getAllLoans(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
        verify(loanRepository).findAll(pageable);
    }

    @Test
    void getLoansByUserId_ShouldReturnUserLoans() {
        // Given
        Long userId = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        when(loanRepository.findByUserId(userId, pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.getLoansByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(userId);
        verify(loanRepository).findByUserId(userId, pageable);
    }

    @Test
    void getActiveLoansByUserId_ShouldReturnActiveLoans() {
        // Given
        Long userId = 100L;
        testLoan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findActiveLoansByUserId(userId)).thenReturn(List.of(testLoan));
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        List<LoanDto> result = loanService.getActiveLoansByUserId(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(loanRepository).findActiveLoansByUserId(userId);
    }

    @Test
    void approveLoan_WithPendingLoan_ShouldApproveLoan() {
        // Given
        Long loanId = 1L;
        Long approvedBy = 200L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan approvedLoan = testLoan.toBuilder()
            .status(LoanStatus.APPROVED)
            .approvedBy(approvedBy)
            .approvalDate(LocalDate.now())
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(approvedLoan);
        when(loanMapper.toDto(approvedLoan)).thenReturn(testLoanDto.toBuilder().status(LoanStatus.APPROVED).build());

        // When
        LoanDto result = loanService.approveLoan(loanId, approvedBy);

        // Then
        assertThat(result).isNotNull();
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void approveLoan_WithNonPendingLoan_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        Long approvedBy = 200L;
        testLoan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, () -> loanService.approveLoan(loanId, approvedBy));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectLoan_WithPendingLoan_ShouldRejectLoan() {
        // Given
        Long loanId = 1L;
        String rejectionReason = "Insufficient credit score";
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan rejectedLoan = testLoan.toBuilder()
            .status(LoanStatus.REJECTED)
            .rejectionReason(rejectionReason)
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(rejectedLoan);
        when(loanMapper.toDto(rejectedLoan)).thenReturn(testLoanDto.toBuilder().status(LoanStatus.REJECTED).build());

        // When
        LoanDto result = loanService.rejectLoan(loanId, rejectionReason);

        // Then
        assertThat(result).isNotNull();
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void disburseLoan_WithApprovedLoan_ShouldDisburseLoan() {
        // Given
        Long loanId = 1L;
        testLoan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan disbursedLoan = testLoan.toBuilder()
            .status(LoanStatus.ACTIVE)
            .disbursementDate(LocalDate.now())
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(disbursedLoan);
        when(loanMapper.toDto(disbursedLoan)).thenReturn(testLoanDto.toBuilder().status(LoanStatus.ACTIVE).build());

        // When
        LoanDto result = loanService.disburseLoan(loanId);

        // Then
        assertThat(result).isNotNull();
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void disburseLoan_WithNonApprovedLoan_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, () -> loanService.disburseLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void makePayment_WithActiveLoan_ShouldProcessPayment() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("2000.00");
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setOutstandingBalance(new BigDecimal("50000.00"));
        testLoan.setTotalPaid(BigDecimal.ZERO);
        
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan updatedLoan = testLoan.toBuilder()
            .outstandingBalance(new BigDecimal("48000.00"))
            .totalPaid(new BigDecimal("2000.00"))
            .nextPaymentDate(LocalDate.now().plusMonths(1))
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(updatedLoan);
        when(loanMapper.toDto(updatedLoan)).thenReturn(testLoanDto.toBuilder()
            .outstandingBalance(new BigDecimal("48000.00"))
            .totalPaid(new BigDecimal("2000.00"))
            .build());

        // When
        LoanDto result = loanService.makePayment(loanId, paymentAmount, paymentMethod);

        // Then
        assertThat(result).isNotNull();
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void makePayment_WithInactiveLoan_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("2000.00");
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.PENDING);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, 
            () -> loanService.makePayment(loanId, paymentAmount, paymentMethod));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void makePayment_WithNegativeAmount_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("-100.00");
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, 
            () -> loanService.makePayment(loanId, paymentAmount, paymentMethod));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void calculateMonthlyPayment_WithValidInputs_ShouldCalculateCorrectly() {
        // Given
        BigDecimal principal = new BigDecimal("50000.00");
        BigDecimal interestRate = new BigDecimal("12.0000");
        Integer termMonths = 24;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void calculateMonthlyPayment_WithZeroInterest_ShouldReturnPrincipalDividedByTerm() {
        // Given
        BigDecimal principal = new BigDecimal("50000.00");
        BigDecimal interestRate = BigDecimal.ZERO;
        Integer termMonths = 25;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("2000.00"));
    }

    @Test
    void calculateMonthlyPayment_WithZeroPrincipal_ShouldReturnZero() {
        // Given
        BigDecimal principal = BigDecimal.ZERO;
        BigDecimal interestRate = new BigDecimal("12.0000");
        Integer termMonths = 24;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getTotalOutstandingBalance_ShouldReturnTotalBalance() {
        // Given
        Long userId = 100L;
        BigDecimal expectedBalance = new BigDecimal("75000.00");
        when(loanRepository.calculateTotalOutstandingBalanceByUserId(userId)).thenReturn(expectedBalance);

        // When
        BigDecimal result = loanService.getTotalOutstandingBalance(userId);

        // Then
        assertThat(result).isEqualTo(expectedBalance);
        verify(loanRepository).calculateTotalOutstandingBalanceByUserId(userId);
    }

    @Test
    void getOverdueLoans_ShouldReturnOverdueLoans() {
        // Given
        testLoan.setNextPaymentDate(LocalDate.now().minusDays(5));
        when(loanRepository.findOverdueLoans(any(LocalDate.class))).thenReturn(List.of(testLoan));
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        List<LoanDto> result = loanService.getOverdueLoans();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(loanRepository).findOverdueLoans(any(LocalDate.class));
    }

    @Test
    void getOverdueLoans_WithNoOverdueLoans_ShouldReturnEmptyList() {
        // Given
        when(loanRepository.findOverdueLoans(any(LocalDate.class))).thenReturn(List.of());

        // When
        List<LoanDto> result = loanService.getOverdueLoans();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loanRepository).findOverdueLoans(any(LocalDate.class));
    }

    @Test
    void getLoansByStatus_ShouldReturnLoansWithStatus() {
        // Given
        LoanStatus status = LoanStatus.PENDING;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        when(loanRepository.findByStatus(status, pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.getLoansByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(loanRepository).findByStatus(status, pageable);
    }

    @Test
    void getLoansByType_ShouldReturnLoansWithType() {
        // Given
        LoanType loanType = LoanType.PERSONAL;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        when(loanRepository.findByLoanType(loanType, pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.getLoansByType(loanType, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(loanRepository).findByLoanType(loanType, pageable);
    }

    @Test
    void rejectLoan_WithNonPendingLoan_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        String rejectionReason = "Insufficient credit score";
        testLoan.setStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, () -> loanService.rejectLoan(loanId, rejectionReason));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectLoan_WithNullRejectionReason_ShouldRejectLoan() {
        // Given
        Long loanId = 1L;
        String rejectionReason = null;
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan rejectedLoan = testLoan.toBuilder()
            .status(LoanStatus.REJECTED)
            .rejectionReason(rejectionReason)
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(rejectedLoan);
        when(loanMapper.toDto(rejectedLoan)).thenReturn(testLoanDto.toBuilder().status(LoanStatus.REJECTED).build());

        // When
        LoanDto result = loanService.rejectLoan(loanId, rejectionReason);

        // Then
        assertThat(result).isNotNull();
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void makePayment_WithZeroAmount_ShouldThrowException() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = BigDecimal.ZERO;
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));

        // When & Then
        assertThrows(LoanOperationException.class, 
            () -> loanService.makePayment(loanId, paymentAmount, paymentMethod));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void makePayment_WithPaymentExceedingOutstandingBalance_ShouldSetBalanceToZero() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("60000.00"); // More than outstanding balance
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setOutstandingBalance(new BigDecimal("50000.00"));
        testLoan.setTotalPaid(BigDecimal.ZERO);
        
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan updatedLoan = testLoan.toBuilder()
            .outstandingBalance(BigDecimal.ZERO)
            .totalPaid(new BigDecimal("50000.00"))
            .status(LoanStatus.PAID_OFF)
            .nextPaymentDate(null)
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(updatedLoan);
        when(loanMapper.toDto(updatedLoan)).thenReturn(testLoanDto.toBuilder()
            .outstandingBalance(BigDecimal.ZERO)
            .totalPaid(new BigDecimal("50000.00"))
            .status(LoanStatus.PAID_OFF)
            .build());

        // When
        LoanDto result = loanService.makePayment(loanId, paymentAmount, paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOutstandingBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PAID_OFF);
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void makePayment_WithExactOutstandingBalance_ShouldMarkLoanAsPaidOff() {
        // Given
        Long loanId = 1L;
        BigDecimal paymentAmount = new BigDecimal("50000.00"); // Exact outstanding balance
        String paymentMethod = "BANK_TRANSFER";
        
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setOutstandingBalance(new BigDecimal("50000.00"));
        testLoan.setTotalPaid(BigDecimal.ZERO);
        
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan updatedLoan = testLoan.toBuilder()
            .outstandingBalance(BigDecimal.ZERO)
            .totalPaid(new BigDecimal("50000.00"))
            .status(LoanStatus.PAID_OFF)
            .nextPaymentDate(null)
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(updatedLoan);
        when(loanMapper.toDto(updatedLoan)).thenReturn(testLoanDto.toBuilder()
            .outstandingBalance(BigDecimal.ZERO)
            .totalPaid(new BigDecimal("50000.00"))
            .status(LoanStatus.PAID_OFF)
            .build());

        // When
        LoanDto result = loanService.makePayment(loanId, paymentAmount, paymentMethod);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOutstandingBalance()).isEqualTo(BigDecimal.ZERO);
        assertThat(result.getStatus()).isEqualTo(LoanStatus.PAID_OFF);
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void calculateMonthlyPayment_WithNegativePrincipal_ShouldReturnZero() {
        // Given
        BigDecimal principal = new BigDecimal("-50000.00");
        BigDecimal interestRate = new BigDecimal("12.0000");
        Integer termMonths = 24;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateMonthlyPayment_WithZeroTermMonths_ShouldReturnZero() {
        // Given
        BigDecimal principal = new BigDecimal("50000.00");
        BigDecimal interestRate = new BigDecimal("12.0000");
        Integer termMonths = 0;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateMonthlyPayment_WithNegativeTermMonths_ShouldReturnZero() {
        // Given
        BigDecimal principal = new BigDecimal("50000.00");
        BigDecimal interestRate = new BigDecimal("12.0000");
        Integer termMonths = -5;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void calculateMonthlyPayment_WithHighInterestRate_ShouldCalculateCorrectly() {
        // Given
        BigDecimal principal = new BigDecimal("10000.00");
        BigDecimal interestRate = new BigDecimal("25.0000"); // 25% annual rate
        Integer termMonths = 12;

        // When
        BigDecimal result = loanService.calculateMonthlyPayment(principal, interestRate, termMonths);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isGreaterThan(BigDecimal.ZERO);
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void searchLoans_WithAllCriteria_ShouldReturnFilteredResults() {
        // Given
        Long userId = 100L;
        LoanStatus status = LoanStatus.PENDING;
        LoanType loanType = LoanType.PERSONAL;
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        
        when(loanRepository.searchLoans(userId, status, loanType, fromDate, toDate, pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.searchLoans(userId, status, loanType, fromDate, toDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(loanRepository).searchLoans(userId, status, loanType, fromDate, toDate, pageable);
    }

    @Test
    void searchLoans_WithNullCriteria_ShouldReturnAllResults() {
        // Given
        Long userId = null;
        LoanStatus status = null;
        LoanType loanType = null;
        LocalDate fromDate = null;
        LocalDate toDate = null;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> loanPage = new PageImpl<>(List.of(testLoan));
        
        when(loanRepository.searchLoans(userId, status, loanType, fromDate, toDate, pageable)).thenReturn(loanPage);
        when(loanMapper.toDto(testLoan)).thenReturn(testLoanDto);

        // When
        Page<LoanDto> result = loanService.searchLoans(userId, status, loanType, fromDate, toDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(loanRepository).searchLoans(userId, status, loanType, fromDate, toDate, pageable);
    }

    @Test
    void updateLoanStatus_WithValidLoan_ShouldUpdateStatus() {
        // Given
        Long loanId = 1L;
        LoanStatus newStatus = LoanStatus.ACTIVE;
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(testLoan));
        
        Loan updatedLoan = testLoan.toBuilder()
            .status(newStatus)
            .build();
        
        when(loanRepository.save(any(Loan.class))).thenReturn(updatedLoan);
        when(loanMapper.toDto(updatedLoan)).thenReturn(testLoanDto.toBuilder().status(newStatus).build());

        // When
        LoanDto result = loanService.updateLoanStatus(loanId, newStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(newStatus);
        verify(loanRepository).findById(loanId);
        verify(loanRepository).save(any(Loan.class));
    }

    @Test
    void updateLoanStatus_WithNonExistingLoan_ShouldThrowException() {
        // Given
        Long loanId = 999L;
        LoanStatus newStatus = LoanStatus.ACTIVE;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.updateLoanStatus(loanId, newStatus));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void approveLoan_WithNonExistingLoan_ShouldThrowException() {
        // Given
        Long loanId = 999L;
        Long approvedBy = 200L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.approveLoan(loanId, approvedBy));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void rejectLoan_WithNonExistingLoan_ShouldThrowException() {
        // Given
        Long loanId = 999L;
        String rejectionReason = "Insufficient credit score";
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.rejectLoan(loanId, rejectionReason));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void disburseLoan_WithNonExistingLoan_ShouldThrowException() {
        // Given
        Long loanId = 999L;
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, () -> loanService.disburseLoan(loanId));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void makePayment_WithNonExistingLoan_ShouldThrowException() {
        // Given
        Long loanId = 999L;
        BigDecimal paymentAmount = new BigDecimal("2000.00");
        String paymentMethod = "BANK_TRANSFER";
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(LoanNotFoundException.class, 
            () -> loanService.makePayment(loanId, paymentAmount, paymentMethod));
        verify(loanRepository).findById(loanId);
        verify(loanRepository, never()).save(any());
    }

    @Test
    void getActiveLoansByUserId_WithNoActiveLoans_ShouldReturnEmptyList() {
        // Given
        Long userId = 100L;
        when(loanRepository.findActiveLoansByUserId(userId)).thenReturn(List.of());

        // When
        List<LoanDto> result = loanService.getActiveLoansByUserId(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(loanRepository).findActiveLoansByUserId(userId);
    }

    @Test
    void getLoansByUserId_WithNoLoans_ShouldReturnEmptyPage() {
        // Given
        Long userId = 100L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of());
        when(loanRepository.findByUserId(userId, pageable)).thenReturn(emptyPage);

        // When
        Page<LoanDto> result = loanService.getLoansByUserId(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(loanRepository).findByUserId(userId, pageable);
    }

    @Test
    void getLoansByStatus_WithNoLoans_ShouldReturnEmptyPage() {
        // Given
        LoanStatus status = LoanStatus.REJECTED;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of());
        when(loanRepository.findByStatus(status, pageable)).thenReturn(emptyPage);

        // When
        Page<LoanDto> result = loanService.getLoansByStatus(status, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(loanRepository).findByStatus(status, pageable);
    }

    @Test
    void getLoansByType_WithNoLoans_ShouldReturnEmptyPage() {
        // Given
        LoanType loanType = LoanType.MORTGAGE;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of());
        when(loanRepository.findByLoanType(loanType, pageable)).thenReturn(emptyPage);

        // When
        Page<LoanDto> result = loanService.getLoansByType(loanType, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(loanRepository).findByLoanType(loanType, pageable);
    }

    @Test
    void getAllLoans_WithNoLoans_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of());
        when(loanRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<LoanDto> result = loanService.getAllLoans(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(loanRepository).findAll(pageable);
    }

    @Test
    void searchLoans_WithNoResults_ShouldReturnEmptyPage() {
        // Given
        Long userId = 100L;
        LoanStatus status = LoanStatus.PENDING;
        LoanType loanType = LoanType.PERSONAL;
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Loan> emptyPage = new PageImpl<>(List.of());
        
        when(loanRepository.searchLoans(userId, status, loanType, fromDate, toDate, pageable)).thenReturn(emptyPage);

        // When
        Page<LoanDto> result = loanService.searchLoans(userId, status, loanType, fromDate, toDate, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(loanRepository).searchLoans(userId, status, loanType, fromDate, toDate, pageable);
    }
}
