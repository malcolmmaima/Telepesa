package com.maelcolium.telepesa.transfer.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class FeignConfig {

    @Autowired
    @Qualifier("serviceToken")
    private String serviceToken;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // For internal service calls, use service token
                if (template.url().contains("/internal/")) {
                    template.header("Authorization", "Bearer " + serviceToken);
                    return;
                }
                
                // For regular calls, try to forward user authorization
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null) {
                        template.header("Authorization", authHeader);
                        return;
                    }
                }
                
                // Fallback to service token for inter-service communication
                template.header("Authorization", "Bearer " + serviceToken);
            }
        };
    }
}
