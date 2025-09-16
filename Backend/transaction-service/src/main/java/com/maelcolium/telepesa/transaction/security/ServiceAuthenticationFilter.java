package com.maelcolium.telepesa.transaction.security;

import com.maelcolium.telepesa.models.auth.ServiceAuthToken;
import com.maelcolium.telepesa.models.auth.ServiceAuthenticationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceAuthenticationFilter extends OncePerRequestFilter {

    private final ServiceAuthenticationService serviceAuthService;

    public ServiceAuthenticationFilter(ServiceAuthenticationService serviceAuthService) {
        this.serviceAuthService = serviceAuthService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // Try to validate as service token first
            ServiceAuthToken serviceToken = serviceAuthService.validateServiceToken(token);
            
            if (serviceToken != null && !serviceToken.isExpired()) {
                // Create authentication for service
                List<SimpleGrantedAuthority> authorities = Arrays.stream(serviceToken.getPermissions())
                        .map(permission -> new SimpleGrantedAuthority("SERVICE_" + permission))
                        .collect(Collectors.toList());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        serviceToken.getServiceName(), 
                        null, 
                        authorities
                    );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Service authenticated: " + serviceToken.getServiceName());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
