package com.maelcolium.telepesa.models.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServiceAuthenticationService {

    @Value("${service.auth.secret:telepesa-service-secret-key-2024}")
    private String serviceSecret;

    @Value("${service.auth.expiration:3600}")
    private int tokenExpirationSeconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(serviceSecret.getBytes());
    }

    public String generateServiceToken(String serviceName, String[] permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("service", serviceName);
        claims.put("permissions", permissions);
        claims.put("type", "service");

        Date expirationDate = Date.from(
            LocalDateTime.now()
                .plusSeconds(tokenExpirationSeconds)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public ServiceAuthToken validateServiceToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String serviceName = claims.get("service", String.class);
            String type = claims.get("type", String.class);
            
            if (!"service".equals(type)) {
                return null;
            }

            @SuppressWarnings("unchecked")
            String[] permissions = claims.get("permissions", String[].class);

            LocalDateTime expiresAt = claims.getExpiration()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            return new ServiceAuthToken(token, serviceName, expiresAt, permissions);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isValidServiceToken(String token) {
        ServiceAuthToken serviceToken = validateServiceToken(token);
        return serviceToken != null && !serviceToken.isExpired();
    }
}
