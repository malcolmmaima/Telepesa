package com.maelcolium.telepesa.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankResponse {
    private String id;
    private String bankCode;
    private String bankName;
    private String country;
    private String swift;
    private boolean active;
    private List<String> supportedCurrencies;
    private BankFeatures features;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankFeatures {
        private boolean instantTransfer;
        private boolean scheduledTransfer;
        private boolean internationalTransfer;
        private String maxTransferAmount;
        private String minTransferAmount;
    }
}
