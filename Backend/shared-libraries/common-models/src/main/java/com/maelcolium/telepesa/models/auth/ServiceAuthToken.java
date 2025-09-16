package com.maelcolium.telepesa.models.auth;

import java.time.LocalDateTime;

public class ServiceAuthToken {
    private String token;
    private String serviceName;
    private LocalDateTime expiresAt;
    private String[] permissions;

    public ServiceAuthToken() {}

    public ServiceAuthToken(String token, String serviceName, LocalDateTime expiresAt, String[] permissions) {
        this.token = token;
        this.serviceName = serviceName;
        this.expiresAt = expiresAt;
        this.permissions = permissions;
    }

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String[] getPermissions() { return permissions; }
    public void setPermissions(String[] permissions) { this.permissions = permissions; }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasPermission(String permission) {
        if (permissions == null) return false;
        for (String p : permissions) {
            if (p.equals(permission) || p.equals("*")) {
                return true;
            }
        }
        return false;
    }
}
