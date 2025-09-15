package com.maelcolium.telepesa.transaction.service;

import com.maelcolium.telepesa.transaction.dto.CreateTransactionRequest;
import com.maelcolium.telepesa.transaction.dto.TransactionDto;
import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.transaction.repository.TransactionRepository;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for TransactionService
 * Tests complete transaction workflows with database interactions
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
    "eureka.client.enabled=false",
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.cloud.discovery.enabled=false"
})
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    private CreateTransactionRequest baseRequest;

    @BeforeEach
    void setUp() {
        // Clean up database
        transactionRepository.deleteAll();

        // Setup base request
        baseRequest = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("1000.00"))
                .transactionType(TransactionType.TRANSFER)
                .description("Integration test transaction")
                .userId(100L)
                .build();
    }

    @Test
    void createTransaction_ShouldPersistInDatabase() {
        // When
        TransactionDto result = transactionService.createTransaction(baseRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTransactionId()).isNotNull();
        
        // Verify database persistence
        List<Transaction> allTransactions = transactionRepository.findAll();
        assertThat(allTransactions).hasSize(1);
        
        Transaction savedTransaction = allTransactions.get(0);
        assertThat(savedTransaction.getAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(savedTransaction.getTransactionType()).isEqualTo(TransactionType.TRANSFER);
        assertThat(savedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void createMultipleTransactions_ShouldAllBePersisted() {
        // Given
        CreateTransactionRequest request1 = createRequest(new BigDecimal("500.00"), TransactionType.DEPOSIT);
        CreateTransactionRequest request2 = createRequest(new BigDecimal("750.00"), TransactionType.WITHDRAWAL);
        CreateTransactionRequest request3 = createRequest(new BigDecimal("1200.00"), TransactionType.TRANSFER);

        // When
        TransactionDto result1 = transactionService.createTransaction(request1);
        TransactionDto result2 = transactionService.createTransaction(request2);
        TransactionDto result3 = transactionService.createTransaction(request3);

        // Then
        List<Transaction> allTransactions = transactionRepository.findAll();
        assertThat(allTransactions).hasSize(3);
        
        BigDecimal totalAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAmount).isEqualTo(new BigDecimal("2450.00"));
    }

    @Test
    void getTransactionsByUserId_ShouldReturnUserTransactions() {
        // Given - Create transactions for different users
        createTransactionForUser(100L, new BigDecimal("1000.00"));
        createTransactionForUser(100L, new BigDecimal("500.00"));
        createTransactionForUser(200L, new BigDecimal("750.00"));

        // When
        Page<TransactionDto> user100Transactions = transactionService.getTransactionsByUserId(100L, PageRequest.of(0, 10));
        Page<TransactionDto> user200Transactions = transactionService.getTransactionsByUserId(200L, PageRequest.of(0, 10));

        // Then
        assertThat(user100Transactions.getContent()).hasSize(2);
        assertThat(user200Transactions.getContent()).hasSize(1);
        
        BigDecimal user100Total = user100Transactions.getContent().stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(user100Total).isEqualTo(new BigDecimal("1500.00"));
    }

    @Test
    void getTransactionsByAccountId_ShouldReturnAccountTransactions() {
        // Given
        CreateTransactionRequest fromAccount1 = createRequest(1L, 2L, new BigDecimal("500.00"));
        CreateTransactionRequest toAccount1 = createRequest(3L, 1L, new BigDecimal("300.00"));
        CreateTransactionRequest otherAccount = createRequest(4L, 5L, new BigDecimal("200.00"));

        transactionService.createTransaction(fromAccount1);
        transactionService.createTransaction(toAccount1);
        transactionService.createTransaction(otherAccount);

        // When
        Page<TransactionDto> account1Transactions = transactionService.getTransactionsByAccountId(1L, PageRequest.of(0, 10));

        // Then
        assertThat(account1Transactions.getContent()).hasSize(2);
        assertThat(account1Transactions.getContent())
                .extracting(TransactionDto::getAmount)
                .containsExactlyInAnyOrder(new BigDecimal("500.00"), new BigDecimal("300.00"));
    }

    @Test
    void getTransactionsByStatus_ShouldReturnFilteredResults() {
        // Given
        TransactionDto pendingTxn = transactionService.createTransaction(baseRequest);
        TransactionDto completedTxn = transactionService.createTransaction(baseRequest);
        
        // Update one to completed
        transactionService.updateTransactionStatus(completedTxn.getId(), TransactionStatus.COMPLETED);

        // When
        Page<TransactionDto> pendingTransactions = transactionService.getTransactionsByStatus(
                TransactionStatus.PENDING, PageRequest.of(0, 10));
        Page<TransactionDto> completedTransactions = transactionService.getTransactionsByStatus(
                TransactionStatus.COMPLETED, PageRequest.of(0, 10));

        // Then
        assertThat(pendingTransactions.getContent()).hasSize(1);
        assertThat(completedTransactions.getContent()).hasSize(1);
        assertThat(completedTransactions.getContent().get(0).getProcessedAt()).isNotNull();
    }

    @Test
    void getTransactionsByType_ShouldReturnFilteredResults() {
        // Given
        transactionService.createTransaction(createRequest(new BigDecimal("1000.00"), TransactionType.DEPOSIT));
        transactionService.createTransaction(createRequest(new BigDecimal("500.00"), TransactionType.WITHDRAWAL));
        transactionService.createTransaction(createRequest(new BigDecimal("750.00"), TransactionType.TRANSFER));
        transactionService.createTransaction(createRequest(new BigDecimal("300.00"), TransactionType.TRANSFER));

        // When
        Page<TransactionDto> deposits = transactionService.getTransactionsByType(
                TransactionType.DEPOSIT, PageRequest.of(0, 10));
        Page<TransactionDto> transfers = transactionService.getTransactionsByType(
                TransactionType.TRANSFER, PageRequest.of(0, 10));

        // Then
        assertThat(deposits.getContent()).hasSize(1);
        assertThat(transfers.getContent()).hasSize(2);
        
        BigDecimal transferTotal = transfers.getContent().stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(transferTotal).isEqualTo(new BigDecimal("1050.00"));
    }

    @Test
    void getTransactionsByDateRange_ShouldReturnCorrectResults() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        LocalDateTime endDate = LocalDateTime.now().plusHours(1);
        
        transactionService.createTransaction(baseRequest);
        transactionService.createTransaction(baseRequest);

        // When
        Page<TransactionDto> transactionsInRange = transactionService.getTransactionsByDateRange(
                100L, startDate, endDate, PageRequest.of(0, 10));
        Page<TransactionDto> transactionsOutsideRange = transactionService.getTransactionsByDateRange(
                100L, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1), PageRequest.of(0, 10));

        // Then
        assertThat(transactionsInRange.getContent()).hasSize(2);
        assertThat(transactionsOutsideRange.getContent()).isEmpty();
    }

    @Test
    void updateTransactionStatus_ShouldPersistChanges() {
        // Given
        TransactionDto transaction = transactionService.createTransaction(baseRequest);
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);

        // When
        TransactionDto updatedTransaction = transactionService.updateTransactionStatus(
                transaction.getId(), TransactionStatus.COMPLETED);

        // Then
        assertThat(updatedTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(updatedTransaction.getProcessedAt()).isNotNull();
        
        // Verify in database
        Transaction dbTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        assertThat(dbTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(dbTransaction.getProcessedAt()).isNotNull();
    }

    @Test
    void getAccountBalance_ShouldCalculateCorrectBalance() {
        // Given - Create credits and debits for account
        createDepositTransaction(1L, new BigDecimal("1000.00")); // Credit
        createDepositTransaction(1L, new BigDecimal("500.00"));  // Credit
        createWithdrawalTransaction(1L, new BigDecimal("300.00"));  // Debit
        createWithdrawalTransaction(1L, new BigDecimal("200.00"));  // Debit

        // When
        BigDecimal balance = transactionService.getAccountBalance(1L);

        // Then
        // Credits: 1500, Debits: 500, Balance: 1000
        assertThat(balance).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void getAccountTransactionHistory_ShouldReturnAllTransactions() {
        // Given
        createTransactionForAccount(1L, 2L, new BigDecimal("500.00"));
        createTransactionForAccount(3L, 1L, new BigDecimal("300.00"));
        createTransactionForAccount(1L, 4L, new BigDecimal("200.00"));

        // When
        List<TransactionDto> history = transactionService.getAccountTransactionHistory(1L);

        // Then
        assertThat(history).hasSize(3);
        BigDecimal totalAmount = history.stream()
                .map(TransactionDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalAmount).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void getTotalCreditsByAccountId_ShouldReturnCorrectAmount() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        createDepositTransaction(1L, new BigDecimal("1000.00")); // Credit to account 1
        createDepositTransaction(1L, new BigDecimal("500.00"));  // Credit to account 1
        createWithdrawalTransaction(1L, new BigDecimal("300.00"));  // Debit from account 1

        // When
        BigDecimal totalCredits = transactionService.getTotalCreditsByAccountId(1L, since);

        // Then
        assertThat(totalCredits).isEqualTo(new BigDecimal("1500.00"));
    }

    @Test
    void getTotalDebitsByAccountId_ShouldReturnCorrectAmount() {
        // Given
        LocalDateTime since = LocalDateTime.now().minusDays(1);
        createWithdrawalTransaction(1L, new BigDecimal("800.00"));  // Debit from account 1
        createWithdrawalTransaction(1L, new BigDecimal("200.00"));  // Debit from account 1
        createDepositTransaction(1L, new BigDecimal("500.00"));  // Credit to account 1

        // When
        BigDecimal totalDebits = transactionService.getTotalDebitsByAccountId(1L, since);

        // Then
        assertThat(totalDebits).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void getTransactionCountByUserIdAndStatus_ShouldReturnCorrectCount() {
        // Given
        TransactionDto txn1 = transactionService.createTransaction(createRequestForUser(100L));
        TransactionDto txn2 = transactionService.createTransaction(createRequestForUser(100L));
        TransactionDto txn3 = transactionService.createTransaction(createRequestForUser(200L));
        
        // Complete one transaction
        transactionService.updateTransactionStatus(txn1.getId(), TransactionStatus.COMPLETED);

        // When
        long pendingCount = transactionService.getTransactionCountByUserIdAndStatus(100L, TransactionStatus.PENDING);
        long completedCount = transactionService.getTransactionCountByUserIdAndStatus(100L, TransactionStatus.COMPLETED);

        // Then
        assertThat(pendingCount).isEqualTo(1);
        assertThat(completedCount).isEqualTo(1);
    }

    @Test
    void concurrentTransactionCreation_ShouldAllBePersisted() throws InterruptedException, ExecutionException {
        // Given
        int numberOfTransactions = 20;

        // When - Create concurrent transactions
        List<CompletableFuture<TransactionDto>> futures = IntStream.range(0, numberOfTransactions)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    CreateTransactionRequest request = createRequest(
                            new BigDecimal("100.00"), 
                            TransactionType.TRANSFER
                    );
                    return transactionService.createTransaction(request);
                }))
                .toList();

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();

        // Then
        List<Transaction> allTransactions = transactionRepository.findAll();
        assertThat(allTransactions).hasSize(numberOfTransactions);
        
        // Verify all have unique transaction IDs
        long uniqueTransactionIds = allTransactions.stream()
                .map(Transaction::getTransactionId)
                .distinct()
                .count();
        assertThat(uniqueTransactionIds).isEqualTo(numberOfTransactions);
    }

    @Test
    void transactionFeeCalculation_ShouldBeCorrectForDifferentTypes() {
        // Given
        CreateTransactionRequest depositRequest = createRequest(new BigDecimal("1000.00"), TransactionType.DEPOSIT);
        CreateTransactionRequest withdrawalRequest = createRequest(new BigDecimal("1000.00"), TransactionType.WITHDRAWAL);
        CreateTransactionRequest transferRequest = createRequest(new BigDecimal("1000.00"), TransactionType.TRANSFER);

        // When
        TransactionDto deposit = transactionService.createTransaction(depositRequest);
        TransactionDto withdrawal = transactionService.createTransaction(withdrawalRequest);
        TransactionDto transfer = transactionService.createTransaction(transferRequest);

        // Then
        assertThat(deposit.getFeeAmount()).isEqualTo(BigDecimal.ZERO); // No fee for deposits
        assertThat(withdrawal.getFeeAmount()).isGreaterThan(BigDecimal.ZERO); // Fee for withdrawals
        assertThat(transfer.getFeeAmount()).isGreaterThan(BigDecimal.ZERO); // Fee for transfers
        
        // Verify total amounts include fees
        assertThat(deposit.getTotalAmount()).isEqualTo(deposit.getAmount());
        assertThat(withdrawal.getTotalAmount()).isEqualTo(withdrawal.getAmount().add(withdrawal.getFeeAmount()));
        assertThat(transfer.getTotalAmount()).isEqualTo(transfer.getAmount().add(transfer.getFeeAmount()));
    }

    // Helper methods
    private CreateTransactionRequest createRequest(BigDecimal amount, TransactionType type) {
        return CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(amount)
                .transactionType(type)
                .description("Test transaction")
                .userId(100L)
                .build();
    }

    private CreateTransactionRequest createRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        return CreateTransactionRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(100L)
                .build();
    }

    private CreateTransactionRequest createRequestForUser(Long userId) {
        return CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(new BigDecimal("500.00"))
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(userId)
                .build();
    }

    private void createTransactionForUser(Long userId, BigDecimal amount) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(1L)
                .toAccountId(2L)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(userId)
                .build();
        transactionService.createTransaction(request);
    }

    private void createTransactionForAccount(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .transactionType(TransactionType.TRANSFER)
                .description("Test transaction")
                .userId(100L)
                .build();
        transactionService.createTransaction(request);
    }

    private void createDepositTransaction(Long accountId, BigDecimal amount) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(999L) // System account for deposits
                .toAccountId(accountId)
                .amount(amount)
                .transactionType(TransactionType.DEPOSIT)
                .description("Test deposit")
                .userId(100L)
                .build();
        TransactionDto transaction = transactionService.createTransaction(request);
        // Complete the transaction so it's included in balance calculations
        transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED);
    }

    private void createWithdrawalTransaction(Long accountId, BigDecimal amount) {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .fromAccountId(accountId)
                .toAccountId(998L) // System account for withdrawals
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .description("Test withdrawal")
                .userId(100L)
                .build();
        TransactionDto transaction = transactionService.createTransaction(request);
        // Complete the transaction so it's included in balance calculations
        transactionService.updateTransactionStatus(transaction.getId(), TransactionStatus.COMPLETED);
    }
}
