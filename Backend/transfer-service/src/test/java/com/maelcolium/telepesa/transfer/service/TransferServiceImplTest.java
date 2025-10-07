package com.maelcolium.telepesa.transfer.service;

import com.maelcolium.telepesa.transfer.client.AccountServiceClient;
import com.maelcolium.telepesa.transfer.dto.CreateTransferRequest;
import com.maelcolium.telepesa.transfer.dto.TransferResponse;
import com.maelcolium.telepesa.transfer.entity.Transfer;
import com.maelcolium.telepesa.transfer.repository.TransferRepository;
import com.maelcolium.telepesa.transfer.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.anyString;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountServiceClient accountServiceClient;

    @InjectMocks
    private TransferServiceImpl transferService;

    private CreateTransferRequest createTransferRequest;
    private Transfer transfer;
    private AccountServiceClient.AccountResponse senderAccount;
    private AccountServiceClient.AccountResponse recipientAccount;

    @BeforeEach
    void setUp() {
        createTransferRequest = new CreateTransferRequest();
        createTransferRequest.setRecipientAccountId("recipient-123");
        createTransferRequest.setAmount(new BigDecimal("1000.00"));
        createTransferRequest.setTransferType(Transfer.TransferType.INTERNAL);
        createTransferRequest.setDescription("Test transfer");
        createTransferRequest.setCurrency("KES");

        transfer = new Transfer();
        transfer.setId("transfer-123");
        transfer.setTransferReference("TXN123456789");
        transfer.setSenderAccountId("sender-123");
        transfer.setRecipientAccountId("recipient-123");
        transfer.setAmount(new BigDecimal("1000.00"));
        transfer.setTransferType(Transfer.TransferType.INTERNAL);
        transfer.setStatus(Transfer.TransferStatus.PENDING);
        transfer.setTransferFee(BigDecimal.ZERO);
        transfer.setTotalAmount(new BigDecimal("1000.00"));

        senderAccount = new AccountServiceClient.AccountResponse(
            "sender-123", "ACC-001", new BigDecimal("5000.00"), 
            "KES", "ACTIVE", "user-123", "SAVINGS", "Sender Account"
        );

        recipientAccount = new AccountServiceClient.AccountResponse(
            "recipient-123", "ACC-002", new BigDecimal("2000.00"), 
            "KES", "ACTIVE", "user-456", "SAVINGS", "Recipient Account"
        );
    }

    @Test
    void createTransfer_Success() {
        // Given
        when(accountServiceClient.getAccountByNumber("sender-123")).thenReturn(senderAccount);
        when(accountServiceClient.getAccountByNumber("recipient-123")).thenReturn(recipientAccount);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferRepository.findById("transfer-123")).thenReturn(Optional.of(transfer));

        // When
        TransferResponse result = transferService.createTransfer("sender-123", createTransferRequest);

        // Then
        assertNotNull(result);
        assertEquals("transfer-123", result.getId());
        assertEquals("sender-123", result.getSenderAccountId());
        assertEquals("recipient-123", result.getRecipientAccountId());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        
        verify(accountServiceClient).getAccountByNumber("sender-123");
        verify(accountServiceClient).getAccountByNumber("recipient-123");
        verify(transferRepository, times(3)).save(any(Transfer.class)); // Called 3 times: create, process status update, completion
    }

    @Test
    void createTransfer_InsufficientBalance_ThrowsException() {
        // Given
        AccountServiceClient.AccountResponse lowBalanceAccount = new AccountServiceClient.AccountResponse(
            "sender-123", "ACC-001", new BigDecimal("100.00"), 
            "KES", "ACTIVE", "user-123", "SAVINGS", "Low Balance Account"
        );
        
        when(accountServiceClient.getAccountByNumber("sender-123")).thenReturn(lowBalanceAccount);
        when(accountServiceClient.getAccountByNumber("recipient-123")).thenReturn(recipientAccount);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.createTransfer("sender-123", createTransferRequest)
        );
        
        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    void getTransferById_Success() {
        // Given
        when(transferRepository.findById("transfer-123")).thenReturn(Optional.of(transfer));

        // When
        TransferResponse result = transferService.getTransferById("transfer-123");

        // Then
        assertNotNull(result);
        assertEquals("transfer-123", result.getId());
        assertEquals("TXN123456789", result.getTransferReference());
    }

    @Test
    void getTransferById_NotFound_ThrowsException() {
        // Given
        when(transferRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> transferService.getTransferById("nonexistent")
        );
        
        assertTrue(exception.getMessage().contains("Transfer not found"));
    }

    @Test
    void calculateTransferFee_Internal_ZeroFee() {
        // When
        BigDecimal fee = transferService.calculateTransferFee(
            new BigDecimal("1000.00"), 
            Transfer.TransferType.INTERNAL
        );

        // Then
        assertEquals(BigDecimal.ZERO, fee);
    }

    @Test
    void calculateTransferFee_MobileMoney_CorrectFee() {
        // When
        BigDecimal fee = transferService.calculateTransferFee(
            new BigDecimal("1000.00"), 
            Transfer.TransferType.MOBILE_MONEY
        );

        // Then
        assertEquals(new BigDecimal("10.00"), fee); // 1% with minimum 10
    }

    @Test
    void calculateTransferFee_MobileMoney_MinimumFee() {
        // When
        BigDecimal fee = transferService.calculateTransferFee(
            new BigDecimal("100.00"), 
            Transfer.TransferType.MOBILE_MONEY
        );

        // Then
        assertEquals(new BigDecimal("10.00"), fee); // Should use minimum fee
    }

    @Test
    void cancelTransfer_Success() {
        // Given
        when(transferRepository.findById("transfer-123")).thenReturn(Optional.of(transfer));
        transfer.setStatus(Transfer.TransferStatus.CANCELLED);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        // When
        TransferResponse result = transferService.cancelTransfer("transfer-123", "User requested");

        // Then
        assertNotNull(result);
        assertEquals(Transfer.TransferStatus.CANCELLED, result.getStatus());
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void cancelTransfer_AlreadyCompleted_ThrowsException() {
        // Given
        transfer.setStatus(Transfer.TransferStatus.COMPLETED);
        when(transferRepository.findById("transfer-123")).thenReturn(Optional.of(transfer));

        // When & Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> transferService.cancelTransfer("transfer-123", "Cannot cancel")
        );
        
        assertTrue(exception.getMessage().contains("Cannot cancel completed transfer"));
    }
}
