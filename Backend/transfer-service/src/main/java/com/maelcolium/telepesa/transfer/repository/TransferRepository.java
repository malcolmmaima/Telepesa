package com.maelcolium.telepesa.transfer.repository;

import com.maelcolium.telepesa.transfer.entity.Transfer;
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
public interface TransferRepository extends JpaRepository<Transfer, String> {
    
    Optional<Transfer> findByTransferReference(String transferReference);
    
    Page<Transfer> findBySenderAccountIdOrderByCreatedAtDesc(String senderAccountId, Pageable pageable);
    
    Page<Transfer> findByRecipientAccountIdOrderByCreatedAtDesc(String recipientAccountId, Pageable pageable);
    
    @Query("SELECT t FROM Transfer t WHERE (t.senderAccountId = :accountId OR t.recipientAccountId = :accountId) ORDER BY t.createdAt DESC")
    Page<Transfer> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") String accountId, Pageable pageable);
    
    List<Transfer> findByStatusInAndCreatedAtBefore(List<Transfer.TransferStatus> statuses, LocalDateTime cutoffTime);
    
    @Query("SELECT t FROM Transfer t WHERE t.status = :status AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Transfer> findByStatusAndDateRange(
        @Param("status") Transfer.TransferStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT SUM(t.amount) FROM Transfer t WHERE t.senderAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :since")
    BigDecimal getTotalSentAmount(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM Transfer t WHERE t.senderAccountId = :accountId AND t.status = 'COMPLETED' AND t.createdAt >= :since")
    Long getTransferCountBySender(@Param("accountId") String accountId, @Param("since") LocalDateTime since);
    
    @Query("SELECT t FROM Transfer t WHERE t.transferType = :transferType AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transfer> findByTransferTypeAndStatus(
        @Param("transferType") Transfer.TransferType transferType,
        @Param("status") Transfer.TransferStatus status,
        Pageable pageable
    );
    
    boolean existsByTransferReference(String transferReference);
}
