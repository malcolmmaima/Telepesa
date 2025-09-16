package com.maelcolium.telepesa.transaction.controller;

import com.maelcolium.telepesa.models.auth.ServiceAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class ServiceTokenTestController {

    @Autowired
    private ServiceAuthenticationService serviceAuthService;

    @GetMapping("/health")
    public String health() {
        return "Transaction service is running";
    }

    @GetMapping("/generate-service-token")
    public String generateServiceToken() {
        String[] permissions = {"TRANSACTION_WRITE", "TRANSACTION_READ"};
        String token = serviceAuthService.generateServiceToken("transfer-service", permissions);
        return "Service token: " + token;
    }
}
