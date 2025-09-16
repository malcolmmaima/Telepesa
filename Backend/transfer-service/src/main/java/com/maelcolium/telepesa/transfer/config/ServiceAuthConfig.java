package com.maelcolium.telepesa.transfer.config;

import com.maelcolium.telepesa.models.auth.ServiceAuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceAuthConfig {

    @Value("${service.name:transfer-service}")
    private String serviceName;

    @Bean
    public ServiceAuthenticationService serviceAuthenticationService() {
        return new ServiceAuthenticationService();
    }

    @Bean
    public String serviceToken(ServiceAuthenticationService serviceAuthService) {
        // Generate service token with transaction write permissions
        String[] permissions = {"TRANSACTION_WRITE", "TRANSACTION_READ"};
        return serviceAuthService.generateServiceToken(serviceName, permissions);
    }
}
