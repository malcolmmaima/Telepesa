package com.maelcolium.telepesa.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.reactive.function.client.WebClient;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    @Value("${app.jwt.secret:default-secret-key-for-development}")
    private String jwtSecret;

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(
            jwtSecret.getBytes(StandardCharsets.UTF_8), 
            "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - allow all actuator endpoints
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/health/**").permitAll()
                .pathMatchers("/api/v1/docs/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers("/api/v1/users/register").permitAll()
                .pathMatchers("/api/v1/users/login").permitAll()
                .pathMatchers("/api/v1/users/verify/**").permitAll()
                .pathMatchers("/api/v1/users/reset-password/**").permitAll()
                .pathMatchers("/api/v1/users/forgot-password/**").permitAll()
                .pathMatchers("/api/v1/notifications/public/**").permitAll()
                .pathMatchers("/api/v1/status/**").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/favicon.ico").permitAll()
                .pathMatchers("/error").permitAll()
                .pathMatchers("/").permitAll()
                // Protected endpoints - require authentication
                .pathMatchers("/api/v1/users/**").authenticated()
                .pathMatchers("/api/v1/accounts/**").authenticated()
                .pathMatchers("/api/v1/transactions/**").authenticated()
                .pathMatchers("/api/v1/loans/**").authenticated()
                .pathMatchers("/api/v1/notifications/**").authenticated()
                .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder())))
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * Configures WebClient for service-to-service communication
     * 
     * @return WebClient with appropriate timeout and connection settings
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
            .build();
    }
}
