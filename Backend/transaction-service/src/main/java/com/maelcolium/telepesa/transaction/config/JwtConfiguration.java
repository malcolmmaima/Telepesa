package com.maelcolium.telepesa.transaction.config;

import com.maelcolium.telepesa.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Configuration for Transaction Service
 * Creates JWT utility bean using shared library
 */
@Configuration
public class JwtConfiguration {

    @Bean
    public JwtTokenUtil jwtTokenUtil(
            @Value("${app.jwt.secret:mySecretKey}") String jwtSecret,
            @Value("${app.jwt.expiration:86400}") int jwtExpiration) {
        return new JwtTokenUtil(jwtSecret, jwtExpiration);
    }
}
