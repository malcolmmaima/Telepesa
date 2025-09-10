package com.maelcolium.telepesa.bill.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Telepesa Bill Payment Service
 */
@SpringBootApplication(scanBasePackages = "com.maelcolium.telepesa")
@EnableJpaAuditing
@EnableCaching
@EnableFeignClients
@EnableDiscoveryClient
@EnableTransactionManagement
public class BillPaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillPaymentServiceApplication.class, args);
    }
}
