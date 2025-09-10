package com.maelcolium.telepesa.transfer.service;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferStatsResponse {
    
    private String accountId;
    private Long totalTransfersSent;
    private BigDecimal totalAmountSent;
    private Long totalTransfersReceived;
    private BigDecimal totalAmountReceived;
    private BigDecimal totalFeesCharged;
}
