package com.maelcolium.telepesa.transaction.config;

import com.maelcolium.telepesa.transaction.security.JwtAuthenticationFilter;
import com.maelcolium.telepesa.transaction.security.ServiceAuthenticationFilter;
import com.maelcolium.telepesa.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private ServiceAuthenticationFilter serviceAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Temporarily allow all for development
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // Add service authentication filter first (for service-to-service calls)
        http.addFilterBefore(serviceAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // Add JWT filter for user authentication
        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService), 
                           UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
