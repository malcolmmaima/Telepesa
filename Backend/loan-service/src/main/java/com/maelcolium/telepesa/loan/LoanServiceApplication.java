package com.maelcolium.telepesa.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Telepesa Loan Service.
 * 
 * This service handles:
 * - Loan applications and approvals
 * - Credit scoring and risk assessment
 * - Loan disbursement and repayment tracking
 * - Interest calculations and payment schedules
 * - Loan portfolio management
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableTransactionManagement
@EnableFeignClients
@EnableDiscoveryClient
public class LoanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
    }
} 