package com.maelcolium.telepesa.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Account Service.
 * 
 * This service handles:
 * - Bank account creation and management
 * - Account balance tracking
 * - Account status management
 * - Account verification and compliance
 * - Account types (Savings, Checking, etc.)
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@SpringBootApplication(scanBasePackages = {
    "com.maelcolium.telepesa.account",
    "com.maelcolium.telepesa.security",
    "com.maelcolium.telepesa.exceptions"
})
@EnableFeignClients
@EnableJpaAuditing
@EnableTransactionManagement
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
} 