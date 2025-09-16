package com.maelcolium.telepesa.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionPinResponse {
    private Long id;
    private Long userId;
    private boolean isSet;
    private String createdAt;
    private String updatedAt;
}


