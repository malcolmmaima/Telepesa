package com.maelcolium.telepesa.transfer.service;

import com.maelcolium.telepesa.transfer.dto.CreateTransferRequest;
import com.maelcolium.telepesa.transfer.dto.TransferResponse;
import com.maelcolium.telepesa.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransferService {
    
    /**
     * Create a new transfer
     */
    TransferResponse createTransfer(String senderAccountId, CreateTransferRequest request);
    
    /**
     * Get transfer by ID
     */
    TransferResponse getTransferById(String transferId);
    
    /**
     * Get transfer by reference
     */
    TransferResponse getTransferByReference(String transferReference);
    
    /**
     * Get transfers by account (sent or received)
     */
    Page<TransferResponse> getTransfersByAccount(String accountId, Pageable pageable);
    
    /**
     * Get sent transfers by account
     */
    Page<TransferResponse> getSentTransfers(String senderAccountId, Pageable pageable);
    
    /**
     * Get received transfers by account
     */
    Page<TransferResponse> getReceivedTransfers(String recipientAccountId, Pageable pageable);
    
    /**
     * Process pending transfer
     */
    TransferResponse processTransfer(String transferId);
    
    /**
     * Cancel transfer
     */
    TransferResponse cancelTransfer(String transferId, String reason);
    
    /**
     * Get transfer statistics for an account
     */
    TransferStatsResponse getTransferStats(String accountId, LocalDateTime since);
    
    /**
     * Retry failed transfer
     */
    TransferResponse retryTransfer(String transferId);
    
    /**
     * Get transfers by status
     */
    List<TransferResponse> getTransfersByStatus(Transfer.TransferStatus status, int limit);
    
    /**
     * Calculate transfer fee
     */
    BigDecimal calculateTransferFee(BigDecimal amount, Transfer.TransferType transferType);
}
