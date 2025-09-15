package com.maelcolium.telepesa.loan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cloud.netflix.eureka.EnableEurekaClient; // Deprecated in newer Spring Cloud versions
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
@SpringBootApplication(
    scanBasePackages = {
        "com.maelcolium.telepesa.loan",
        "com.maelcolium.telepesa.models",
        "com.maelcolium.telepesa.security"
    },
    exclude = {
        org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class
    }
)
@EnableJpaRepositories(basePackages = "com.maelcolium.telepesa.loan.repository")
@EnableAsync
@EnableTransactionManagement
@EnableFeignClients
// @EnableEurekaClient // Deprecated - Eureka client is auto-configured when dependency is present
public class LoanServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanServiceApplication.class, args);
    }
} 