package com.maelcolium.telepesa.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientResponse {
    private String id;
    private String userId;
    private String recipientName;
    private String recipientEmail;
    private String recipientPhone;
    private String bankCode;
    private String bankName;
    private String accountNumber;
    private String accountType;
    private String currency;
    private boolean isVerified;
    private boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsed;
    private int transferCount;
}
