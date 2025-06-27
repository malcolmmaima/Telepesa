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

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    Page<Transaction> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId, Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    Page<Transaction> findByUserIdAndProcessedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByProcessedAtDesc(Long fromAccountId, Long toAccountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) AND t.status = 'COMPLETED'")
    BigDecimal calculateAccountBalance(@Param("accountId") Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.fromAccountId = :accountId AND t.status = 'COMPLETED' AND t.processedAt >= :since")
    BigDecimal getTotalDebitsByAccountIdSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.toAccountId = :accountId AND t.status = 'COMPLETED' AND t.processedAt >= :since")
    BigDecimal getTotalCreditsByAccountIdSince(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    long countByUserIdAndStatus(Long userId, TransactionStatus status);

    boolean existsByTransactionId(String transactionId);

    boolean existsByReferenceNumber(String referenceNumber);
} 