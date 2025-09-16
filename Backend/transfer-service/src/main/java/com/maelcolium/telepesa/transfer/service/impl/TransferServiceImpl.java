package com.maelcolium.telepesa.transfer.service.impl;

import com.maelcolium.telepesa.transfer.client.AccountServiceClient;
import com.maelcolium.telepesa.transfer.client.TransactionServiceClient;
import com.maelcolium.telepesa.transfer.dto.CreateTransferRequest;
import com.maelcolium.telepesa.transfer.dto.TransferResponse;
import com.maelcolium.telepesa.transfer.entity.Transfer;
import com.maelcolium.telepesa.transfer.repository.TransferRepository;
import com.maelcolium.telepesa.transfer.service.TransferService;
import com.maelcolium.telepesa.transfer.service.TransferStatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransferServiceImpl implements TransferService {
    
    private final TransferRepository transferRepository;
    private final AccountServiceClient accountServiceClient;
    private final TransactionServiceClient transactionServiceClient;
    
    @Override
    public TransferResponse createTransfer(String senderAccountId, CreateTransferRequest request) {
        log.info("Creating transfer from {} to {} for amount {}", 
                senderAccountId, request.getRecipientAccountId(), request.getAmount());
        
        // Validate sender account exists
        AccountServiceClient.AccountResponse senderAccount = accountServiceClient.getAccountByNumber(senderAccountId);
        if ("UNAVAILABLE".equals(senderAccount.status())) {
            throw new IllegalArgumentException("Sender account not found or unavailable: " + senderAccountId);
        }
        
        // Validate recipient account exists
        AccountServiceClient.AccountResponse recipientAccount = accountServiceClient.getAccountByNumber(request.getRecipientAccountId());
        if ("UNAVAILABLE".equals(recipientAccount.status())) {
            throw new IllegalArgumentException("Recipient account not found or unavailable: " + request.getRecipientAccountId());
        }
        
        // Calculate fees
        BigDecimal transferFee = calculateTransferFee(request.getAmount(), request.getTransferType());
        BigDecimal totalAmount = request.getAmount().add(transferFee);
        
        // Check if sender has sufficient balance
        if (senderAccount.balance().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Required: " + totalAmount + 
                ", Available: " + senderAccount.balance());
        }
        
        // Create transfer entity
        Transfer transfer = new Transfer();
        transfer.setSenderAccountId(senderAccountId);
        transfer.setRecipientAccountId(request.getRecipientAccountId());
        transfer.setAmount(request.getAmount());
        transfer.setCurrency(request.getCurrency());
        transfer.setTransferType(request.getTransferType());
        transfer.setDescription(request.getDescription());
        transfer.setReference(request.getReference());
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setSenderName(senderAccount.accountName() != null ? senderAccount.accountName() : "Account Holder");
        transfer.setRecipientName(request.getRecipientName());
        transfer.setRecipientPhoneNumber(request.getRecipientPhoneNumber());
        transfer.setTransferReference(generateTransferReference());
        
        // Set transfer type specific fields
        transfer.setSwiftCode(request.getSwiftCode());
        transfer.setRecipientBankName(request.getRecipientBankName());
        transfer.setRecipientBankAddress(request.getRecipientBankAddress());
        transfer.setIntermediaryBankSwift(request.getIntermediaryBankSwift());
        transfer.setSortCode(request.getSortCode());
        transfer.setPesalinkBankCode(request.getPesalinkBankCode());
        transfer.setMpesaNumber(request.getMpesaNumber());
        
        // Use the already calculated fee
        transfer.setTransferFee(transferFee);
        transfer.setTotalAmount(totalAmount);
        
        // Save transfer
        Transfer savedTransfer = transferRepository.save(transfer);
        
        // Process transfer based on type
        switch (request.getTransferType()) {
            case INTERNAL:
                return processTransfer(savedTransfer.getId());
            case PESALINK:
                return processPesaLinkTransfer(savedTransfer);
            case MPESA:
                return processMpesaTransfer(savedTransfer);
            case RTGS:
                return processRTGSTransfer(savedTransfer);
            case SWIFT:
                return processSWIFTTransfer(savedTransfer);
            default:
                // For other types, mark as processing and return
                savedTransfer.setStatus(Transfer.TransferStatus.PROCESSING);
                transferRepository.save(savedTransfer);
                return mapToResponse(savedTransfer);
        }
    }
    
    @Override
    @Cacheable(value = "transfers", key = "#transferId")
    public TransferResponse getTransferById(String transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        return mapToResponse(transfer);
    }
    
    @Override
    @Cacheable(value = "transfers", key = "#transferReference")
    public TransferResponse getTransferByReference(String transferReference) {
        Transfer transfer = transferRepository.findByTransferReference(transferReference)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferReference));
        return mapToResponse(transfer);
    }
    
    @Override
    @Cacheable(value = "accountTransfers", key = "#accountId + '_' + #pageable.pageNumber")
    public Page<TransferResponse> getTransfersByAccount(String accountId, Pageable pageable) {
        return transferRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Cacheable(value = "sentTransfers", key = "#senderAccountId + '_' + #pageable.pageNumber")
    public Page<TransferResponse> getSentTransfers(String senderAccountId, Pageable pageable) {
        return transferRepository.findBySenderAccountIdOrderByCreatedAtDesc(senderAccountId, pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @Cacheable(value = "receivedTransfers", key = "#recipientAccountId + '_' + #pageable.pageNumber")
    public Page<TransferResponse> getReceivedTransfers(String recipientAccountId, Pageable pageable) {
        return transferRepository.findByRecipientAccountIdOrderByCreatedAtDesc(recipientAccountId, pageable)
                .map(this::mapToResponse);
    }
    
    @Override
    @CacheEvict(value = {"transfers", "accountTransfers", "sentTransfers", "receivedTransfers"}, allEntries = true)
    public TransferResponse processTransfer(String transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        
        if (transfer.getStatus() != Transfer.TransferStatus.PENDING) {
            throw new IllegalStateException("Transfer is not in PENDING status: " + transfer.getStatus());
        }
        
        transfer.setStatus(Transfer.TransferStatus.PROCESSING);
        transfer.setProcessedAt(LocalDateTime.now());
        transferRepository.save(transfer);
        
        try {
            // Debit sender account
            AccountServiceClient.DebitRequest debitRequest = new AccountServiceClient.DebitRequest(
                transfer.getTotalAmount(),
                transfer.getCurrency(),
                transfer.getTransferReference(),
                "Transfer to " + transfer.getRecipientAccountId()
            );
            
            AccountServiceClient.TransactionResponse debitResponse = 
                accountServiceClient.debitAccount(transfer.getSenderAccountId(), debitRequest);
            
            if (!"COMPLETED".equals(debitResponse.status())) {
                throw new RuntimeException("Failed to debit sender account: " + debitResponse.status());
            }
            
            // Credit recipient account (only for INTERNAL transfers)
            if (transfer.getTransferType() == Transfer.TransferType.INTERNAL) {
                AccountServiceClient.CreditRequest creditRequest = new AccountServiceClient.CreditRequest(
                    transfer.getAmount(), // Don't include fee for recipient
                    transfer.getCurrency(),
                    transfer.getTransferReference(),
                    "Transfer from " + transfer.getSenderAccountId()
                );
                
                AccountServiceClient.TransactionResponse creditResponse = 
                    accountServiceClient.creditAccount(transfer.getRecipientAccountId(), creditRequest);
                
                if (!"COMPLETED".equals(creditResponse.status())) {
                    // Reverse the debit transaction
                    log.error("Failed to credit recipient account. Reversing debit transaction.");
                    // In real scenario, implement compensation transaction
                    transfer.setStatus(Transfer.TransferStatus.FAILED);
                    transfer.setFailureReason("Failed to credit recipient account: " + creditResponse.status());
                    transferRepository.save(transfer);
                    return mapToResponse(transfer);
                }
            }
            
            // Mark as completed
            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transfer.setProcessedBy("SYSTEM");
            transfer.setProcessedAt(LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("Transfer processing failed for {}: {}", transferId, e.getMessage(), e);
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason(e.getMessage());
        }
        
        Transfer updatedTransfer = transferRepository.save(transfer);
        return mapToResponse(updatedTransfer);
    }
    
    @Override
    @CacheEvict(value = {"transfers", "accountTransfers", "sentTransfers", "receivedTransfers"}, allEntries = true)
    public TransferResponse cancelTransfer(String transferId, String reason) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        
        if (transfer.getStatus() == Transfer.TransferStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed transfer");
        }
        
        transfer.setStatus(Transfer.TransferStatus.CANCELLED);
        transfer.setFailureReason(reason);
        transfer.setProcessedAt(LocalDateTime.now());
        
        Transfer updatedTransfer = transferRepository.save(transfer);
        return mapToResponse(updatedTransfer);
    }
    
    @Override
    @Cacheable(value = "transferStats", key = "#accountId + '_' + #since")
    public TransferStatsResponse getTransferStats(String accountId, LocalDateTime since) {
        BigDecimal totalSent = transferRepository.getTotalSentAmount(accountId, since);
        Long sentCount = transferRepository.getTransferCountBySender(accountId, since);
        
        // For received transfers, we'd need additional queries
        // Simplified for now
        return new TransferStatsResponse(
            accountId,
            sentCount,
            totalSent != null ? totalSent : BigDecimal.ZERO,
            0L, // placeholder for received count
            BigDecimal.ZERO, // placeholder for received amount
            BigDecimal.ZERO // placeholder for fees
        );
    }
    
    @Override
    public TransferResponse retryTransfer(String transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        
        if (transfer.getStatus() != Transfer.TransferStatus.FAILED) {
            throw new IllegalStateException("Can only retry failed transfers");
        }
        
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setFailureReason(null);
        transferRepository.save(transfer);
        
        return processTransfer(transferId);
    }
    
    @Override
    public List<TransferResponse> getTransfersByStatus(Transfer.TransferStatus status, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transferRepository.findByTransferTypeAndStatus(null, status, pageable)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    
    @Override
    public BigDecimal calculateTransferFee(BigDecimal amount, Transfer.TransferType transferType) {
        // Simple fee calculation logic
        BigDecimal feePercentage;
        BigDecimal minimumFee;
        BigDecimal maximumFee;
        
        switch (transferType) {
            case INTERNAL:
                return BigDecimal.ZERO; // No fee for internal transfers
            case PESALINK:
                return new BigDecimal("25.00"); // Fixed fee for PesaLink
            case MPESA:
                return new BigDecimal("15.00"); // Fixed fee for M-Pesa
            case RTGS:
                return new BigDecimal("500.00"); // Fixed fee for RTGS
            case SWIFT:
                return new BigDecimal("25.00"); // Fixed fee for SWIFT (USD)
            case MOBILE_MONEY:
                feePercentage = new BigDecimal("0.01"); // 1%
                minimumFee = new BigDecimal("10.00");
                maximumFee = new BigDecimal("200.00");
                break;
            case BANK_TRANSFER:
                feePercentage = new BigDecimal("0.005"); // 0.5%
                minimumFee = new BigDecimal("20.00");
                maximumFee = new BigDecimal("500.00");
                break;
            case PEER_TO_PEER:
                feePercentage = new BigDecimal("0.002"); // 0.2%
                minimumFee = new BigDecimal("5.00");
                maximumFee = new BigDecimal("100.00");
                break;
            default:
                return BigDecimal.ZERO;
        }
        
        BigDecimal calculatedFee = amount.multiply(feePercentage).setScale(2, RoundingMode.HALF_UP);
        
        if (calculatedFee.compareTo(minimumFee) < 0) {
            return minimumFee;
        } else if (calculatedFee.compareTo(maximumFee) > 0) {
            return maximumFee;
        } else {
            return calculatedFee;
        }
    }
    
    private TransferResponse processPesaLinkTransfer(Transfer transfer) {
        try {
            // Debit sender account
            accountServiceClient.debitAccount(transfer.getSenderAccountId(), 
                new AccountServiceClient.DebitRequest(transfer.getTotalAmount(), 
                    "PesaLink transfer to " + transfer.getRecipientName()));
            
            // TODO: Integrate with PesaLink API
            // For now, simulate successful processing
            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transfer.setProcessedAt(LocalDateTime.now());
            transfer.setProcessedBy("PESALINK_GATEWAY");
            
            Transfer savedTransfer = transferRepository.save(transfer);
            return mapToResponse(savedTransfer);
            
        } catch (Exception e) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("PesaLink processing failed: " + e.getMessage());
            transferRepository.save(transfer);
            throw new RuntimeException("PesaLink transfer failed", e);
        }
    }
    
    private TransferResponse processMpesaTransfer(Transfer transfer) {
        try {
            // Debit sender account
            accountServiceClient.debitAccount(transfer.getSenderAccountId(), 
                new AccountServiceClient.DebitRequest(transfer.getTotalAmount(), 
                    "M-Pesa transfer to " + transfer.getMpesaNumber()));
            
            // TODO: Integrate with M-Pesa API (Daraja API)
            // For now, simulate successful processing
            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transfer.setProcessedAt(LocalDateTime.now());
            transfer.setProcessedBy("MPESA_GATEWAY");
            
            Transfer savedTransfer = transferRepository.save(transfer);
            return mapToResponse(savedTransfer);
            
        } catch (Exception e) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("M-Pesa processing failed: " + e.getMessage());
            transferRepository.save(transfer);
            throw new RuntimeException("M-Pesa transfer failed", e);
        }
    }
    
    private TransferResponse processRTGSTransfer(Transfer transfer) {
        try {
            // Credit recipient account
            accountServiceClient.creditAccount(transfer.getRecipientAccountId(), 
                new AccountServiceClient.CreditRequest(transfer.getAmount(), 
                    "Transfer from " + transfer.getSenderName()));
            
            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transfer.setProcessedAt(LocalDateTime.now());
            
            Transfer completedTransfer = transferRepository.save(transfer);
            
            // Create transaction records for both sender and recipient
            createTransactionRecords(completedTransfer);
            
            return mapToResponse(completedTransfer);
            
        } catch (Exception e) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("RTGS processing failed: " + e.getMessage());
            transferRepository.save(transfer);
            throw new RuntimeException("RTGS transfer failed", e);
        }
    }
    
    private TransferResponse processSWIFTTransfer(Transfer transfer) {
        try {
            // Debit sender account
            accountServiceClient.debitAccount(transfer.getSenderAccountId(), 
                new AccountServiceClient.DebitRequest(transfer.getTotalAmount(), 
                    "SWIFT transfer to " + transfer.getRecipientBankName()));
            
            // TODO: Integrate with SWIFT network
            // For now, mark as processing (SWIFT takes 1-3 business days)
            transfer.setStatus(Transfer.TransferStatus.PROCESSING);
            transfer.setProcessedBy("SWIFT_GATEWAY");
            
            Transfer savedTransfer = transferRepository.save(transfer);
            return mapToResponse(savedTransfer);
            
        } catch (Exception e) {
            transfer.setStatus(Transfer.TransferStatus.FAILED);
            transfer.setFailureReason("SWIFT processing failed: " + e.getMessage());
            transferRepository.save(transfer);
            throw new RuntimeException("SWIFT transfer failed", e);
        }
    }
    
    private void createTransactionRecords(Transfer transfer) {
        try {
            // Create debit transaction for sender
            transactionServiceClient.createTransaction(
                new TransactionServiceClient.CreateTransactionRequest(
                    Long.parseLong(transfer.getSenderAccountId()),
                    transfer.getAmount().negate(), // Negative for debit
                    "TRANSFER",
                    "Transfer to " + transfer.getRecipientName(),
                    Long.parseLong(transfer.getRecipientAccountId()),
                    transfer.getRecipientAccountId(),
                    transfer.getTransferReference(),
                    transfer.getTransferFee(),
                    transfer.getTotalAmount().negate(),
                    transfer.getCurrency()
                )
            );
            
            // Create credit transaction for recipient (only for internal transfers)
            if (transfer.getTransferType() == Transfer.TransferType.INTERNAL) {
                transactionServiceClient.createTransaction(
                    new TransactionServiceClient.CreateTransactionRequest(
                        Long.parseLong(transfer.getRecipientAccountId()),
                        transfer.getAmount(), // Positive for credit
                        "TRANSFER",
                        "Transfer from " + transfer.getSenderName(),
                        Long.parseLong(transfer.getSenderAccountId()),
                        transfer.getSenderAccountId(),
                        transfer.getTransferReference(),
                        BigDecimal.ZERO,
                        transfer.getAmount(),
                        transfer.getCurrency()
                    )
                );
            }
        } catch (Exception e) {
            log.warn("Failed to create transaction records for transfer {}: {}", 
                transfer.getId(), e.getMessage());
            // Don't fail the transfer if transaction recording fails
        }
    }
    
    private String generateTransferReference() {
        return "TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
    
    private TransferResponse mapToResponse(Transfer transfer) {
        TransferResponse response = new TransferResponse();
        response.setId(transfer.getId());
        response.setTransferReference(transfer.getTransferReference());
        response.setSenderAccountId(transfer.getSenderAccountId());
        response.setRecipientAccountId(transfer.getRecipientAccountId());
        response.setAmount(transfer.getAmount());
        response.setCurrency(transfer.getCurrency());
        response.setTransferType(transfer.getTransferType());
        response.setStatus(transfer.getStatus());
        response.setDescription(transfer.getDescription());
        response.setReference(transfer.getReference());
        response.setTransferFee(transfer.getTransferFee());
        response.setTotalAmount(transfer.getTotalAmount());
        response.setSenderName(transfer.getSenderName());
        response.setRecipientName(transfer.getRecipientName());
        response.setSenderPhoneNumber(transfer.getSenderPhoneNumber());
        response.setRecipientPhoneNumber(transfer.getRecipientPhoneNumber());
        response.setCreatedAt(transfer.getCreatedAt());
        response.setUpdatedAt(transfer.getUpdatedAt());
        response.setProcessedAt(transfer.getProcessedAt());
        response.setFailureReason(transfer.getFailureReason());
        return response;
    }
}
