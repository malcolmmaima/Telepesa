package com.maelcolium.telepesa.transaction.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.service.TransactionService;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import com.maelcolium.telepesa.exceptions.ResourceNotFoundException;
import com.maelcolium.telepesa.transaction.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;
    @InjectMocks
    private TransactionController transactionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TransactionDto transactionDto;
    private CreateTransactionRequest createRequest;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper with required modules
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        
        // Configure Jackson converter
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        transactionDto = TransactionDto.builder()
                .id(1L)
                .transactionId("TXN-12345678")
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description("Test transaction")
                .referenceNumber("REF-1234")
                .userId(10L)
                .feeAmount(new BigDecimal("1.00"))
                .totalAmount(new BigDecimal("101.00"))
                .processedAt(LocalDateTime.now())
                .build();

        createRequest = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("100.00"))
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(10L)
                .build();
    }

    @Test
    void createTransaction_WithValidRequest_ShouldReturnCreatedTransaction() throws Exception {
        // Given
        when(transactionService.createTransaction(any(CreateTransactionRequest.class))).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345678"))
                .andExpect(jsonPath("$.amount").value("100.0"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(transactionService).createTransaction(any(CreateTransactionRequest.class));
    }

    @Test
    void createTransaction_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        CreateTransactionRequest invalidRequest = CreateTransactionRequest.builder()
                .fromAccountId(null)
                .amount(null)
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTransaction_WithValidId_ShouldReturnTransaction() throws Exception {
        // Given
        when(transactionService.getTransaction(1L)).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345678"))
                .andExpect(jsonPath("$.amount").value("100.0"));

        verify(transactionService).getTransaction(1L);
    }

    @Test
    void getTransaction_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(transactionService.getTransaction(999L))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Transaction not found"));

        verify(transactionService).getTransaction(999L);
    }

    @Test
    void getTransactionByTransactionId_WithValidId_ShouldReturnTransaction() throws Exception {
        // Given
        when(transactionService.getTransactionByTransactionId("TXN-12345678")).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/by-transaction-id/TXN-12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345678"));

        verify(transactionService).getTransactionByTransactionId("TXN-12345678");
    }

    @Test
    void getTransactions_ShouldReturnPagedResults() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        when(transactionService.getTransactions(any(PageRequest.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].transactionId").value("TXN-12345678"));

        verify(transactionService).getTransactions(any(PageRequest.class));
    }

    @Test
    void getTransactionsByUserId_ShouldReturnUserTransactions() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        when(transactionService.getTransactionsByUserId(10L, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/user/10")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].userId").value(10));

        verify(transactionService).getTransactionsByUserId(10L, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByAccountId_ShouldReturnAccountTransactions() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        when(transactionService.getTransactionsByAccountId(1L, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(transactionService).getTransactionsByAccountId(1L, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByStatus_ShouldReturnFilteredTransactions() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        when(transactionService.getTransactionsByStatus(TransactionStatus.PENDING, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/status/PENDING")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(transactionService).getTransactionsByStatus(TransactionStatus.PENDING, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByType_ShouldReturnFilteredTransactions() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        when(transactionService.getTransactionsByType(TransactionType.TRANSFER, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/type/TRANSFER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(transactionService).getTransactionsByType(TransactionType.TRANSFER, PageRequest.of(0, 10));
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnFilteredTransactions() throws Exception {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<TransactionDto> page = new PageImpl<>(List.of(transactionDto), pageRequest, 1);
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(transactionService.getTransactionsByDateRange(10L, startDate, endDate, PageRequest.of(0, 10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/user/10/date-range")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(transactionService).getTransactionsByDateRange(10L, startDate, endDate, PageRequest.of(0, 10));
    }

    @Test
    void updateTransactionStatus_WithValidRequest_ShouldReturnUpdatedTransaction() throws Exception {
        // Given
        when(transactionService.updateTransactionStatus(1L, TransactionStatus.COMPLETED)).thenReturn(transactionDto);

        // When & Then
        mockMvc.perform(put("/api/v1/transactions/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"COMPLETED\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TXN-12345678"));

        verify(transactionService).updateTransactionStatus(1L, TransactionStatus.COMPLETED);
    }

    @Test
    void updateTransactionStatus_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Given
        when(transactionService.updateTransactionStatus(999L, TransactionStatus.COMPLETED))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/transactions/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"COMPLETED\""))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Transaction not found"));

        verify(transactionService).updateTransactionStatus(999L, TransactionStatus.COMPLETED);
    }

    @Test
    void getAccountTransactionHistory_ShouldReturnTransactionList() throws Exception {
        // Given
        when(transactionService.getAccountTransactionHistory(1L)).thenReturn(List.of(transactionDto));

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].transactionId").value("TXN-12345678"));

        verify(transactionService).getAccountTransactionHistory(1L);
    }

    @Test
    void getAccountBalance_ShouldReturnBalance() throws Exception {
        // Given
        when(transactionService.getAccountBalance(1L)).thenReturn(new BigDecimal("150.0"));

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("150.0"));

        verify(transactionService).getAccountBalance(1L);
    }

    @Test
    void getTotalDebitsByAccountId_ShouldReturnTotalDebits() throws Exception {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionService.getTotalDebitsByAccountId(1L, since)).thenReturn(new BigDecimal("100.0"));

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/1/debits")
                        .param("since", since.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("100.0"));

        verify(transactionService).getTotalDebitsByAccountId(1L, since);
    }

    @Test
    void getTotalCreditsByAccountId_ShouldReturnTotalCredits() throws Exception {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        when(transactionService.getTotalCreditsByAccountId(1L, since)).thenReturn(new BigDecimal("200.0"));

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/account/1/credits")
                        .param("since", since.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("200.0"));

        verify(transactionService).getTotalCreditsByAccountId(1L, since);
    }

    @Test
    void getTransactionCountByUserIdAndStatus_ShouldReturnCount() throws Exception {
        // Given
        when(transactionService.getTransactionCountByUserIdAndStatus(10L, TransactionStatus.COMPLETED)).thenReturn(5L);

        // When & Then
        mockMvc.perform(get("/api/v1/transactions/user/10/count")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));

        verify(transactionService).getTransactionCountByUserIdAndStatus(10L, TransactionStatus.COMPLETED);
    }
} 