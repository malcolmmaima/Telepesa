package com.maelcolium.telepesa.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Audit logging service for banking compliance and security monitoring
 * Logs all security-sensitive operations for regulatory compliance
 */
@Slf4j
@Service
public class AuditLogService {

    /**
     * Log user authentication events
     */
    public void logAuthenticationAttempt(String username, String ipAddress, boolean successful, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "AUTHENTICATION_ATTEMPT");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("successful", successful);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("reason", reason);
        
        if (successful) {
            log.info("AUDIT: Successful login - Username: {}, IP: {}, Time: {}", 
                username, ipAddress, LocalDateTime.now());
        } else {
            log.warn("AUDIT: Failed login attempt - Username: {}, IP: {}, Reason: {}, Time: {}", 
                username, ipAddress, reason, LocalDateTime.now());
        }
        
        // In production, this would be sent to a centralized audit system
        writeAuditLog(auditData);
    }

    /**
     * Log user registration events
     */
    public void logUserRegistration(String username, String email, String ipAddress, boolean successful, String reason) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "USER_REGISTRATION");
        auditData.put("username", username);
        auditData.put("email", email);
        auditData.put("ip_address", ipAddress);
        auditData.put("successful", successful);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("reason", reason);
        
        log.info("AUDIT: User registration - Username: {}, Email: {}, IP: {}, Success: {}, Time: {}", 
            username, email, ipAddress, successful, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log account lockout events
     */
    public void logAccountLockout(String username, String ipAddress, int failedAttempts) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "ACCOUNT_LOCKOUT");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("failed_attempts", failedAttempts);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.warn("AUDIT: Account locked - Username: {}, IP: {}, Failed attempts: {}, Time: {}", 
            username, ipAddress, failedAttempts, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log password change events
     */
    public void logPasswordChange(String username, String ipAddress, boolean successful) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "PASSWORD_CHANGE");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("successful", successful);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.info("AUDIT: Password change - Username: {}, IP: {}, Success: {}, Time: {}", 
            username, ipAddress, successful, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log user profile updates
     */
    public void logUserProfileUpdate(String username, String ipAddress, String fieldsUpdated) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "PROFILE_UPDATE");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("fields_updated", fieldsUpdated);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.info("AUDIT: Profile update - Username: {}, IP: {}, Fields: {}, Time: {}", 
            username, ipAddress, fieldsUpdated, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log administrative actions
     */
    public void logAdministrativeAction(String adminUsername, String targetUsername, String action, String ipAddress) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "ADMINISTRATIVE_ACTION");
        auditData.put("admin_username", adminUsername);
        auditData.put("target_username", targetUsername);
        auditData.put("action", action);
        auditData.put("ip_address", ipAddress);
        auditData.put("timestamp", LocalDateTime.now());
        
        log.warn("AUDIT: Admin action - Admin: {}, Target: {}, Action: {}, IP: {}, Time: {}", 
            adminUsername, targetUsername, action, ipAddress, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log security violations
     */
    public void logSecurityViolation(String username, String ipAddress, String violationType, String details) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "SECURITY_VIOLATION");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("violation_type", violationType);
        auditData.put("details", details);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("severity", "HIGH");
        
        log.error("AUDIT: SECURITY VIOLATION - Username: {}, IP: {}, Type: {}, Details: {}, Time: {}", 
            username, ipAddress, violationType, details, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String username, String ipAddress, String activityType, String description) {
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("event_type", "SUSPICIOUS_ACTIVITY");
        auditData.put("username", username);
        auditData.put("ip_address", ipAddress);
        auditData.put("activity_type", activityType);
        auditData.put("description", description);
        auditData.put("timestamp", LocalDateTime.now());
        auditData.put("severity", "MEDIUM");
        
        log.warn("AUDIT: Suspicious activity - Username: {}, IP: {}, Type: {}, Description: {}, Time: {}", 
            username, ipAddress, activityType, description, LocalDateTime.now());
        
        writeAuditLog(auditData);
    }

    /**
     * Write audit log to persistent storage
     * In production, this would integrate with centralized logging systems like ELK stack
     */
    private void writeAuditLog(Map<String, Object> auditData) {
        // TODO: In production, implement one or more of:
        // 1. Write to audit database table
        // 2. Send to centralized logging system (ELK, Splunk, etc.)
        // 3. Send to security information and event management (SIEM) system
        // 4. Write to dedicated audit log files with rotation
        // 5. Send to external audit service for compliance
        
        // For now, we're using structured logging
        // This ensures audit events are captured in application logs
        // and can be processed by log aggregation systems
        
        log.info("AUDIT_EVENT: {}", auditData);
    }
} 