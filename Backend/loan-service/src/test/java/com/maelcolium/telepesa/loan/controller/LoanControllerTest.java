package com.maelcolium.telepesa.loan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maelcolium.telepesa.loan.dto.CreateLoanRequest;
import com.maelcolium.telepesa.loan.dto.LoanDto;
import com.maelcolium.telepesa.loan.exception.LoanNotFoundException;
import com.maelcolium.telepesa.loan.exception.LoanOperationException;
import com.maelcolium.telepesa.loan.service.LoanService;
import com.maelcolium.telepesa.models.enums.LoanStatus;
import com.maelcolium.telepesa.models.enums.LoanType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for LoanController
 */
@WebMvcTest(LoanController.class)
@ActiveProfiles("test")
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createLoan_WithValidRequest_ShouldReturnCreatedLoan() throws Exception {
        // Given
        when(loanService.createLoan(any(CreateLoanRequest.class))).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLoanRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loanNumber").value("PL202412001234"))
                .andExpect(jsonPath("$.userId").value(100L))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.principalAmount").value(50000.00))
                .andExpect(jsonPath("$.loanType").value("PERSONAL"));
    }

    @Test
    void createLoan_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        createLoanRequest.setPrincipalAmount(null); // Invalid - required field

        // When & Then
        mockMvc.perform(post("/api/v1/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLoanRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getLoan_WithExistingId_ShouldReturnLoan() throws Exception {
        // Given
        when(loanService.getLoan(1L)).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(get("/api/v1/loans/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.loanNumber").value("PL202412001234"))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void getLoan_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Given
        when(loanService.getLoan(999L)).thenThrow(new LoanNotFoundException("Loan not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/loans/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Loan Not Found"));
    }

    @Test
    void getLoanByNumber_WithExistingNumber_ShouldReturnLoan() throws Exception {
        // Given
        when(loanService.getLoanByNumber("PL202412001234")).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(get("/api/v1/loans/number/PL202412001234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanNumber").value("PL202412001234"))
                .andExpect(jsonPath("$.userId").value(100L));
    }

    @Test
    void getAllLoans_ShouldReturnPageOfLoans() throws Exception {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getAllLoans(any())).thenReturn(loanPage);

        // When & Then
        mockMvc.perform(get("/api/v1/loans")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLoansByUserId_ShouldReturnUserLoans() throws Exception {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getLoansByUserId(eq(100L), any())).thenReturn(loanPage);

        // When & Then
        mockMvc.perform(get("/api/v1/loans/user/100")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(100L));
    }

    @Test
    void getActiveLoansByUserId_ShouldReturnActiveLoans() throws Exception {
        // Given
        testLoanDto.setStatus(LoanStatus.ACTIVE);
        when(loanService.getActiveLoansByUserId(100L)).thenReturn(List.of(testLoanDto));

        // When & Then
        mockMvc.perform(get("/api/v1/loans/user/100/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].userId").value(100L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void getLoansByStatus_ShouldReturnLoansWithStatus() throws Exception {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.getLoansByStatus(eq(LoanStatus.PENDING), any())).thenReturn(loanPage);

        // When & Then
        mockMvc.perform(get("/api/v1/loans/status/PENDING")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void approveLoan_WithValidRequest_ShouldApproveLoan() throws Exception {
        // Given
        testLoanDto.setStatus(LoanStatus.APPROVED);
        when(loanService.approveLoan(1L, 200L)).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(put("/api/v1/loans/1/approve")
                .param("approvedBy", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void approveLoan_WithInvalidLoan_ShouldReturnBadRequest() throws Exception {
        // Given
        when(loanService.approveLoan(999L, 200L)).thenThrow(new LoanOperationException("Cannot approve loan"));

        // When & Then
        mockMvc.perform(put("/api/v1/loans/999/approve")
                .param("approvedBy", "200"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectLoan_WithValidRequest_ShouldRejectLoan() throws Exception {
        // Given
        testLoanDto.setStatus(LoanStatus.REJECTED);
        when(loanService.rejectLoan(eq(1L), anyString())).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(put("/api/v1/loans/1/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\": \"Insufficient income\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void disburseLoan_WithValidRequest_ShouldDisburseLoan() throws Exception {
        // Given
        testLoanDto.setStatus(LoanStatus.ACTIVE);
        when(loanService.disburseLoan(1L)).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(post("/api/v1/loans/1/disburse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void makePayment_WithValidRequest_ShouldProcessPayment() throws Exception {
        // Given
        when(loanService.makePayment(eq(1L), eq(new BigDecimal("2347.50")), eq("BANK_TRANSFER"))).thenReturn(testLoanDto);

        // When & Then
        mockMvc.perform(post("/api/v1/loans/1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\": 2347.50, \"paymentMethod\": \"BANK_TRANSFER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getTotalOutstandingBalance_ShouldReturnBalance() throws Exception {
        // Given
        when(loanService.getTotalOutstandingBalance(100L)).thenReturn(new BigDecimal("50000.00"));

        // When & Then
        mockMvc.perform(get("/api/v1/loans/user/100/outstanding-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outstandingBalance").value(50000.00));
    }

    @Test
    void getOverdueLoans_ShouldReturnOverdueLoans() throws Exception {
        // Given
        when(loanService.getOverdueLoans()).thenReturn(List.of(testLoanDto));

        // When & Then
        mockMvc.perform(get("/api/v1/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void searchLoans_WithCriteria_ShouldReturnFilteredResults() throws Exception {
        // Given
        Page<LoanDto> loanPage = new PageImpl<>(List.of(testLoanDto), PageRequest.of(0, 20), 1);
        when(loanService.searchLoans(eq(100L), eq(LoanStatus.PENDING), eq(LoanType.PERSONAL), any(), any(), any())).thenReturn(loanPage);

        // When & Then
        mockMvc.perform(get("/api/v1/loans/search")
                .param("userId", "100")
                .param("status", "PENDING")
                .param("loanType", "PERSONAL")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(100L));
    }
}
