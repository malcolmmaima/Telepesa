package com.maelcolium.telepesa.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Eureka Server Application for Telepesa Microservices
 * 
 * This service acts as the service registry for all Telepesa microservices.
 * It enables service discovery and load balancing across the microservices architecture.
 * 
 * @author Telepesa Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
} 