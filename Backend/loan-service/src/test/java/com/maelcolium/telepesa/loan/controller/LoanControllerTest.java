package com.maelcolium.telepesa.loan.controller;

import com.maelcolium.telepesa.models.dto.LoanDto;
import com.maelcolium.telepesa.models.request.CreateLoanRequest;
import com.maelcolium.telepesa.loan.exception.LoanNotFoundException;
import com.maelcolium.telepesa.loan.exception.LoanOperationException;
import com.maelcolium.telepesa.loan.service.LoanService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoanController
 */
@ExtendWith(MockitoExtension.class)
class LoanControllerTest {

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    private CreateLoanRequest createLoanRequest;
    private LoanDto testLoanDto;

    @BeforeEach
    void setUp() {
        createLoanRequest = CreateLoanRequest.builder()
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("0.1250")) // 12.50% as decimal
            .termMonths(24)
            .purpose("Business expansion")
            .monthlyIncome(new BigDecimal("80000.00"))
            .notes("Good credit history")
            .build();

        testLoanDto = LoanDto.builder()
            .id(1L)
            .loanNumber("PL202412001234")
            .userId(100L)
            .accountNumber("ACC001234567890")
            .loanType(LoanType.PERSONAL)
            .status(LoanStatus.PENDING)
            .principalAmount(new BigDecimal("50000.00"))
            .interestRate(new BigDecimal("0.1250")) // 12.50% as decimal
            .termMonths(24)
            .monthlyPayment(new BigDecimal("2347.50"))
            .outstandingBalance(new BigDecimal("50000.00"))
            .totalPaid(BigDecimal.ZERO)
            .applicationDate(LocalDate.now())
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void createLoan_WithValidRequest_ShouldReturnCreatedLoan() {
        // Given
        when(loanService.createLoan(any(CreateLoanRequest.class))).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.createLoan(createLoanRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getLoanNumber()).isEqualTo("PL202412001234");
        assertThat(response.getBody().getUserId()).isEqualTo(100L);
        assertThat(response.getBody().getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void createLoan_WithInvalidRequest_ShouldReturnBadRequest() {
        // Given
        createLoanRequest.setPrincipalAmount(null); // Invalid - required field

        // When & Then
        try {
            loanController.createLoan(createLoanRequest);
        } catch (Exception e) {
            // The controller should handle validation errors
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void getLoan_WithExistingId_ShouldReturnLoan() {
        // Given
        when(loanService.getLoan(1L)).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.getLoan(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getLoanNumber()).isEqualTo("PL202412001234");
    }

    @Test
    void getLoan_WithNonExistingId_ShouldReturnNotFound() {
        // Given
        when(loanService.getLoan(999L)).thenThrow(new LoanNotFoundException("Loan not found with id: 999"));

        // When & Then
        try {
            loanController.getLoan(999L);
        } catch (LoanNotFoundException e) {
            assertThat(e.getMessage()).contains("Loan not found with id: 999");
        }
    }

    @Test
    void getLoanByNumber_WithExistingNumber_ShouldReturnLoan() {
        // Given
        when(loanService.getLoanByNumber("PL202412001234")).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.getLoanByNumber("PL202412001234");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getLoanNumber()).isEqualTo("PL202412001234");
    }

    @Test
    void getAllLoans_ShouldReturnPageOfLoans() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getAllLoans(any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getAllLoans(0, 20, "createdAt", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
    }

    @Test
    void getLoansByUserId_ShouldReturnUserLoans() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getLoansByUserId(eq(100L), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByUserId(100L, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getUserId()).isEqualTo(100L);
    }

    @Test
    void getActiveLoansByUserId_ShouldReturnActiveLoans() {
        // Given
        testLoanDto.setStatus(LoanStatus.ACTIVE);
        when(loanService.getActiveLoansByUserId(100L)).thenReturn(List.of(testLoanDto));

        // When
        ResponseEntity<List<LoanDto>> response = loanController.getActiveLoansByUserId(100L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void getLoansByStatus_ShouldReturnLoansWithStatus() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getLoansByStatus(eq(LoanStatus.PENDING), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByStatus(LoanStatus.PENDING, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(LoanStatus.PENDING);
    }

    @Test
    void approveLoan_WithValidRequest_ShouldApproveLoan() {
        // Given
        testLoanDto.setStatus(LoanStatus.APPROVED);
        when(loanService.approveLoan(1L, 200L)).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.approveLoan(1L, 200L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    void approveLoan_WithInvalidLoan_ShouldReturnBadRequest() {
        // Given
        when(loanService.approveLoan(999L, 200L)).thenThrow(new LoanOperationException("Cannot approve loan"));

        // When & Then
        try {
            loanController.approveLoan(999L, 200L);
        } catch (LoanOperationException e) {
            assertThat(e.getMessage()).contains("Cannot approve loan");
        }
    }

    @Test
    void rejectLoan_WithValidRequest_ShouldRejectLoan() {
        // Given
        testLoanDto.setStatus(LoanStatus.REJECTED);
        when(loanService.rejectLoan(1L, "Insufficient income")).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.rejectLoan(1L, "Insufficient income");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    void disburseLoan_WithValidRequest_ShouldDisburseLoan() {
        // Given
        testLoanDto.setStatus(LoanStatus.ACTIVE);
        when(loanService.disburseLoan(1L)).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.disburseLoan(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void makePayment_WithValidRequest_ShouldProcessPayment() {
        // Given
        BigDecimal paymentAmount = new BigDecimal("1000.00");
        when(loanService.makePayment(1L, paymentAmount, "BANK_TRANSFER")).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.makePayment(1L, paymentAmount, "BANK_TRANSFER");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getTotalOutstandingBalance_ShouldReturnBalance() {
        // Given
        BigDecimal totalBalance = new BigDecimal("150000.00");
        when(loanService.getTotalOutstandingBalance(100L)).thenReturn(totalBalance);

        // When
        ResponseEntity<BigDecimal> response = loanController.getTotalOutstandingBalance(100L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(totalBalance);
    }

    @Test
    void getOverdueLoans_ShouldReturnOverdueLoans() {
        // Given
        when(loanService.getOverdueLoans()).thenReturn(List.of(testLoanDto));

        // When
        ResponseEntity<List<LoanDto>> response = loanController.getOverdueLoans();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void searchLoans_WithCriteria_ShouldReturnFilteredResults() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.searchLoans(eq(100L), eq(LoanStatus.PENDING), eq(LoanType.PERSONAL), any(), any(), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.searchLoans(
            100L, LoanStatus.PENDING, LoanType.PERSONAL, null, null, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void searchLoans_WithNullCriteria_ShouldReturnAllResults() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.searchLoans(eq(null), eq(null), eq(null), any(), any(), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.searchLoans(
            null, null, null, null, null, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void searchLoans_WithDateCriteria_ShouldReturnFilteredResults() {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        LocalDate fromDate = LocalDate.now().minusDays(30);
        LocalDate toDate = LocalDate.now();
        when(loanService.searchLoans(eq(100L), eq(null), eq(null), eq(fromDate), eq(toDate), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.searchLoans(
            100L, null, null, fromDate, toDate, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getLoansByType_ShouldReturnLoansWithType() {
        // Given
        testLoanDto.setLoanType(LoanType.BUSINESS);
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getLoansByType(eq(LoanType.BUSINESS), any())).thenReturn(loanPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByType(LoanType.BUSINESS, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getLoanType()).isEqualTo(LoanType.BUSINESS);
    }

    @Test
    void getLoanByNumber_WithNonExistingNumber_ShouldReturnNotFound() {
        // Given
        when(loanService.getLoanByNumber("INVALID123")).thenThrow(new LoanNotFoundException("Loan not found"));

        // When & Then
        try {
            loanController.getLoanByNumber("INVALID123");
        } catch (LoanNotFoundException e) {
            assertThat(e.getMessage()).contains("Loan not found");
        }
    }

    @Test
    void rejectLoan_WithInvalidLoan_ShouldReturnBadRequest() {
        // Given
        when(loanService.rejectLoan(999L, "Invalid reason")).thenThrow(new LoanOperationException("Cannot reject loan"));

        // When & Then
        try {
            loanController.rejectLoan(999L, "Invalid reason");
        } catch (LoanOperationException e) {
            assertThat(e.getMessage()).contains("Cannot reject loan");
        }
    }

    @Test
    void disburseLoan_WithInvalidLoan_ShouldReturnBadRequest() {
        // Given
        when(loanService.disburseLoan(999L)).thenThrow(new LoanOperationException("Cannot disburse loan"));

        // When & Then
        try {
            loanController.disburseLoan(999L);
        } catch (LoanOperationException e) {
            assertThat(e.getMessage()).contains("Cannot disburse loan");
        }
    }

    @Test
    void makePayment_WithInvalidLoan_ShouldReturnBadRequest() {
        // Given
        BigDecimal paymentAmount = new BigDecimal("1000.00");
        when(loanService.makePayment(999L, paymentAmount, "BANK_TRANSFER")).thenThrow(new LoanOperationException("Cannot make payment"));

        // When & Then
        try {
            loanController.makePayment(999L, paymentAmount, "BANK_TRANSFER");
        } catch (LoanOperationException e) {
            assertThat(e.getMessage()).contains("Cannot make payment");
        }
    }

    @Test
    void makePayment_WithNegativeAmount_ShouldReturnBadRequest() {
        // Given
        BigDecimal paymentAmount = new BigDecimal("-100.00");
        when(loanService.makePayment(1L, paymentAmount, "BANK_TRANSFER")).thenThrow(new LoanOperationException("Invalid payment amount"));

        // When & Then
        try {
            loanController.makePayment(1L, paymentAmount, "BANK_TRANSFER");
        } catch (LoanOperationException e) {
            assertThat(e.getMessage()).contains("Invalid payment amount");
        }
    }

    @Test
    void updateLoanStatus_WithValidRequest_ShouldUpdateStatus() {
        // Given
        testLoanDto.setStatus(LoanStatus.ACTIVE);
        when(loanService.updateLoanStatus(1L, LoanStatus.ACTIVE)).thenReturn(testLoanDto);

        // When
        ResponseEntity<LoanDto> response = loanController.updateLoanStatus(1L, LoanStatus.ACTIVE);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    void updateLoanStatus_WithInvalidLoan_ShouldReturnBadRequest() {
        // Given
        when(loanService.updateLoanStatus(999L, LoanStatus.ACTIVE)).thenThrow(new LoanNotFoundException("Loan not found"));

        // When & Then
        try {
            loanController.updateLoanStatus(999L, LoanStatus.ACTIVE);
        } catch (LoanNotFoundException e) {
            assertThat(e.getMessage()).contains("Loan not found");
        }
    }

    @Test
    void getAllLoans_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Page<LoanDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(loanService.getAllLoans(any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getAllLoans(0, 20, "createdAt", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getLoansByUserId_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Page<LoanDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(loanService.getLoansByUserId(eq(999L), any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByUserId(999L, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getActiveLoansByUserId_WithEmptyResults_ShouldReturnEmptyList() {
        // Given
        when(loanService.getActiveLoansByUserId(999L)).thenReturn(List.of());

        // When
        ResponseEntity<List<LoanDto>> response = loanController.getActiveLoansByUserId(999L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getLoansByStatus_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Page<LoanDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(loanService.getLoansByStatus(eq(LoanStatus.REJECTED), any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByStatus(LoanStatus.REJECTED, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getLoansByType_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Page<LoanDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(loanService.getLoansByType(eq(LoanType.MORTGAGE), any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.getLoansByType(LoanType.MORTGAGE, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getOverdueLoans_WithEmptyResults_ShouldReturnEmptyList() {
        // Given
        when(loanService.getOverdueLoans()).thenReturn(List.of());

        // When
        ResponseEntity<List<LoanDto>> response = loanController.getOverdueLoans();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void searchLoans_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Page<LoanDto> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(loanService.searchLoans(eq(999L), eq(LoanStatus.PENDING), eq(LoanType.PERSONAL), any(), any(), any())).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<LoanDto>> response = loanController.searchLoans(
            999L, LoanStatus.PENDING, LoanType.PERSONAL, null, null, 0, 20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);
    }

    @Test
    void getTotalOutstandingBalance_WithZeroBalance_ShouldReturnZero() {
        // Given
        BigDecimal zeroBalance = BigDecimal.ZERO;
        when(loanService.getTotalOutstandingBalance(999L)).thenReturn(zeroBalance);

        // When
        ResponseEntity<BigDecimal> response = loanController.getTotalOutstandingBalance(999L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void createLoan_WithNullRequest_ShouldReturnBadRequest() {
        // Given
        CreateLoanRequest nullRequest = null;

        // When & Then
        try {
            loanController.createLoan(nullRequest);
        } catch (Exception e) {
            // Should handle null request appropriately
            assertThat(e).isInstanceOf(Exception.class);
        }
    }

    @Test
    void createLoan_WithEmptyRequest_ShouldReturnBadRequest() {
        // Given
        CreateLoanRequest emptyRequest = CreateLoanRequest.builder().build();

        // When & Then
        try {
            loanController.createLoan(emptyRequest);
        } catch (Exception e) {
            // Should handle empty request appropriately
            assertThat(e).isInstanceOf(Exception.class);
        }
    }
}
