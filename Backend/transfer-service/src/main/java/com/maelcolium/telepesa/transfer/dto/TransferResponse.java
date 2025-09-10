package com.maelcolium.telepesa.transfer.dto;

import com.maelcolium.telepesa.transfer.entity.Transfer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransferResponse {
    
    private String id;
    private String transferReference;
    private String senderAccountId;
    private String recipientAccountId;
    private BigDecimal amount;
    private String currency;
    private Transfer.TransferType transferType;
    private Transfer.TransferStatus status;
    private String description;
    private String reference;
    private BigDecimal transferFee;
    private BigDecimal totalAmount;
    private String senderName;
    private String recipientName;
    private String senderPhoneNumber;
    private String recipientPhoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private String failureReason;
}
