package com.maelcolium.telepesa.transaction.repository;

import com.maelcolium.telepesa.transaction.model.Transaction;
import com.maelcolium.telepesa.models.enums.TransactionStatus;
import com.maelcolium.telepesa.models.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Basic finders
    Optional<Transaction> findByTransactionId(String transactionId);
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
    
    // Pageable finders
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    Page<Transaction> findByFromAccountId(Long fromAccountId, Pageable pageable);
    Page<Transaction> findByToAccountId(Long toAccountId, Pageable pageable);
    Page<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId, Pageable pageable);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);
    
    // Date range queries
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :userId OR t.toAccountId = :userId) AND t.processedAt BETWEEN :startDate AND :endDate ORDER BY t.processedAt DESC")
    Page<Transaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    // List queries  
    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId ORDER BY t.processedAt DESC")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);
    
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByProcessedAtDesc(Long fromAccountId, Long toAccountId);

    // Aggregation queries
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccountId = :accountId AND t.status = 'COMPLETED' AND t.processedAt >= :since")
    BigDecimal getTotalDebitsByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccountId = :accountId AND t.status = 'COMPLETED' AND t.processedAt >= :since")
    BigDecimal getTotalCreditsByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) AND t.status = 'COMPLETED'")
    BigDecimal calculateAccountBalance(@Param("accountId") Long accountId);

    // Count queries
    long countByUserIdAndStatus(Long userId, TransactionStatus status);

    // Existence checks
    boolean existsByTransactionId(String transactionId);
    boolean existsByReferenceNumber(String referenceNumber);
} 