package com.maelcolium.telepesa.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyPinRequest {
    @NotBlank
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be exactly 4 digits")
    private String pin;
}


