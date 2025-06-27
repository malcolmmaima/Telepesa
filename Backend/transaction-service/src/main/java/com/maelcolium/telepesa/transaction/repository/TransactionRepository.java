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

    Page<Transaction> findByFromAccountId(Long accountId, Pageable pageable);

    Page<Transaction> findByToAccountId(Long accountId, Pageable pageable);

    Page<Transaction> findByFromAccountIdOrToAccountId(Long accountId, Long accountId2, Pageable pageable);

    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);

    Page<Transaction> findByTransactionType(TransactionType transactionType, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate")
    Page<Transaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountId = :accountId OR t.toAccountId = :accountId")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TransactionStatus status);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.fromAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :since")
    BigDecimal getTotalDebitsByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.toAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :since")
    BigDecimal getTotalCreditsByAccountId(@Param("accountId") Long accountId, @Param("since") LocalDateTime since);
} 